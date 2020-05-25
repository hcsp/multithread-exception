package com.github.hcsp;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

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
    public boolean processAllData(List<Object> allData)  {
        int groupSize =
                allData.size() % threadNumber == 0
                        ? allData.size() / threadNumber
                        : allData.size() / threadNumber + 1;
        List<List<Object>> dataGroups = Lists.partition(allData, groupSize);
        ArrayList<Future<Boolean>> futures = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(dataGroups.size());
        for (List<Object> dataGroup : dataGroups) {
            Future submit = executorService.submit(new Callable() {
                @Override
                public Boolean call() throws Exception {
                    dataGroup.forEach(remoteService::processData);
                    return true;
                }
            });
            futures.add(submit);
        }
        for (Future<Boolean> future : futures) {
            try {
                Boolean aBoolean = future.get();
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }
}
