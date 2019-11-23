package com.github.hcsp;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

public class MultiThreadServiceDataProcessor {
    // 线程数量
    private final int threadNumber;
    // 处理数据的远程服务
    private final RemoteService remoteService;
    // 线程异常状态，使用volatile保证该变量的可见性
    private volatile boolean isException = true;

    public MultiThreadServiceDataProcessor(int threadNumber, RemoteService remoteService) {
        this.threadNumber = threadNumber;
        this.remoteService = remoteService;
    }

    // 将所有数据发送至远程服务处理。若所有数据都处理成功（没有任何异常抛出），返回true；
    // 否则只要有任何异常产生，返回false
    public boolean processAllData(List<Object> allData) {
        int groupSize =
                allData.size() % threadNumber == 0
                        ? allData.size() / threadNumber
                        : allData.size() / threadNumber + 1;
        List<List<Object>> dataGroups = Lists.partition(allData, groupSize);

        try {
            List<Thread> threads = new ArrayList<>();
            for (List<Object> dataGroup : dataGroups) {
                // 线程的异常要从线程内部捕获，主线程无法捕获new Thread()类中的异常
                Thread thread = new Thread(() -> {
                    try {
                        dataGroup.forEach(remoteService::processData);
                    } catch (Exception e) {
                        e.printStackTrace();
                        isException = false;
                    }
                });
                thread.start();
                threads.add(thread);
            }

            for (Thread thread : threads) {
                thread.join();
            }
            return isException;
        } catch (Exception e) {
            return isException;
        }
    }
}
