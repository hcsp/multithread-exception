package com.github.hcsp;

/** 一个处理数据的远程服务，如果数据处理成功，则没有任何事情发生，否则，若数据处理失败，抛出异常。 */
public interface RemoteService {
    void processData(Object data);
}
