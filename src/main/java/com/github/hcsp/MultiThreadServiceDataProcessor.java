package com.github.hcsp;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MultiThreadServiceDataProcessor {
    // 线程数量
    private final int threadNumber;
    // 处理数据的远程服务
    private final RemoteService remoteService;

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
        AtomicBoolean hasException = new AtomicBoolean(false);
        try {
            List<Thread> threads = new ArrayList<>();
            for (List<Object> dataGroup : dataGroups) {
                Thread thread = new Thread(() -> dataGroup.forEach(remoteService::processData));
                // 使用 setUncaughtExceptionHandler 线程处理异常
                thread.setUncaughtExceptionHandler((t, e) -> {
                    hasException.set(true);
                    // todo:在实际业务中通过设定线程名,序列化对象实体等方法,写入日志,并补偿发送dataGroup
                    System.out.println("线程出错,数据为" + dataGroup);
                });
                thread.start();
                threads.add(thread);
            }

            for (Thread thread : threads) {
                thread.join();
            }
            return !hasException.get();
        } catch (Exception e) {
            return false;
        }
    }
}
