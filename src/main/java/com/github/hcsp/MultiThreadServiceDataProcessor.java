package com.github.hcsp;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 这个类的bug来源于多线程对异常的处理机制。
 * 某一个子线程出现了异常但是没有被捕获的话不会被外面的主线程捕获到，从而造成异常逃逸。
 *
 * 解决方法;
 *   使用Thread.setUncaughtExceptionHandler(),从外部捕获逃逸出来的异常。
 */
public class MultiThreadServiceDataProcessor {
    // 线程数量
    private final int threadNumber;
    // 处理数据的远程服务
    private final RemoteService remoteService;
    // 处理次数
    private final AtomicInteger resultCount = new AtomicInteger();

    public MultiThreadServiceDataProcessor(int threadNumber, RemoteService remoteService) {
        this.threadNumber = threadNumber;
        this.remoteService = remoteService;
    }

    // 将所有数据发送至远程服务处理。若所有数据都处理成功（没有任何异常抛出），返回true；
    // 否则只要有任何异常产生，返回false
    public boolean processAllData(List<Object> allData) {
        resultCount.getAndSet(0);
        int groupSize =
                allData.size() % threadNumber == 0
                        ? allData.size() / threadNumber
                        : allData.size() / threadNumber + 1;
        List<List<Object>> dataGroups = Lists.partition(allData, groupSize);

        try {
            List<Thread> threads = new ArrayList<>();
            for (List<Object> dataGroup : dataGroups) {
                Thread thread = new Thread(() -> {
                    for (Object o : dataGroup) {
                        remoteService.processData(o);
                        resultCount.incrementAndGet();
                    }
                });
                // 捕获到了任何异常就代表本次处理失败
                thread.setUncaughtExceptionHandler((t, e) -> {
                    System.out.println("线程<" + t.getName() + ">处理任务失败，catch：" + e.getMessage());
                });
                thread.start();
                threads.add(thread);
            }

            for (Thread thread : threads) {
                thread.join();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return resultCount.get() == allData.size();
    }
}
