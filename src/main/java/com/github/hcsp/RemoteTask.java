package com.github.hcsp;

import java.util.List;
import java.util.concurrent.Callable;

public class RemoteTask implements Callable<Object> {

    // 处理数据的远程服务
    private final RemoteService remoteService;
    private final List<Object> allData;

    public RemoteTask(RemoteService remoteService, List<Object> allData) {
        this.remoteService = remoteService;
        this.allData = allData;
    }

    @Override
    public Object call() throws Exception {
        try {
            for (Object allDatum : allData) {
                remoteService.processData(allDatum);
            }
            return null;
        } catch (Exception e) {
            throw new Exception("remote service exception");
        }
    }
}
