package com.rpc.client;

/**
 * @Author: Bojun Ji
 * @Description:
 * @Date: 2018/8/2_11:26 PM
 */
public class RPCClient {

    /**
     * init cglib generator and return a proxy class
     *
     * @param <T>
     * @return
     */
    public static <T> T refer(String serviceName, T target) {
        ClientProxyGenerator clientProxyGenerator = new ClientProxyGenerator(target, serviceName);
        return (T)clientProxyGenerator.getProxy();
    }
}