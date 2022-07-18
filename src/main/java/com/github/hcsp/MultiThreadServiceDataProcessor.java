package com.github.hcsp;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MultiThreadServiceDataProcessor {
    // 线程数量
    private final int threadNumber;
    // 处理数据的远程服务
    private final RemoteService remoteService;
    private final ExecutorService threadPool;

    public MultiThreadServiceDataProcessor(int threadNumber, RemoteService remoteService) {
        this.threadNumber = threadNumber;
        this.remoteService = remoteService;
        this.threadPool = Executors.newFixedThreadPool(threadNumber);
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
            List<Future<Object>> futures = new ArrayList<>();
            for (List<Object> dataGroup : dataGroups) {
                Future<Object> submit = threadPool.submit(() -> {
                    dataGroup.forEach(remoteService::processData);
                    return null;
                });
                futures.add(submit);
            }

            for (Future<Object> future : futures) {
                future.get();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
