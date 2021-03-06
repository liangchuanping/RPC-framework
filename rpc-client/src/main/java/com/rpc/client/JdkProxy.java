package com.rpc.client;

import com.rpc.registry.api.ServiceDiscovery;
import com.rpc.registry.zookeeper.ZKServiceDiscovery;
import org.springframework.util.StringUtils;

import java.lang.reflect.Proxy;

/**
 * @Author: Bojun Ji
 * @Description:
 * @Date: 2018/8/25_12:28 AM
 */
public class JdkProxy implements IProxy {
    private String serviceName;
    private Class<?> interfaceClass;
    private String serverAddress;
    private ServiceDiscovery serviceDiscovery = new ZKServiceDiscovery();


    JdkProxy(String serviceName, Class<?> interfaceClass) {
        this.serviceName = serviceName;
        this.interfaceClass = interfaceClass;
    }

    JdkProxy(String serviceName, Class<?> interfaceClass, String serverAddress) {
        this.serviceName = serviceName;
        this.interfaceClass = interfaceClass;
        this.serverAddress = serverAddress;
    }

    @Override
    public Object getProxy() {
        return StringUtils.isEmpty(this.serverAddress) ? Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, new JdkProxyHandler(serviceName, serviceDiscovery)) : Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, new JdkProxyHandler(serviceName, serviceDiscovery, serverAddress));
    }
}
