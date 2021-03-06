package com.rpc.registry.zookeeper;

import com.rpc.common.util.StringUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: Bojun Ji
 * @Date: Created in 2018-07-12 16:05
 * @Description:
 */
public final class ServiceNameListener implements PathChildrenCacheListener {
    private volatile Map<String, List<String>> serviceMap;

    public ServiceNameListener(Map<String, List<String>> serviceMap) {
        this.serviceMap = serviceMap;
    }

    @Override
    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
        switch (pathChildrenCacheEvent.getType()) {
            case CHILD_ADDED: {
                //add the service with empty list
                String serviceNamePath = StringUtil.getZkSubPath(pathChildrenCacheEvent.getData().getPath(), 1);
                if (!serviceMap.containsKey(serviceNamePath)) {
                    serviceMap.put(serviceNamePath, new ArrayList<>());
                }
                break;
            }
            case CHILD_REMOVED: {
                //remove the service
                serviceMap.remove(StringUtil.getZkSubPath(pathChildrenCacheEvent.getData().getPath(), 1));
                break;
            }
            case CHILD_UPDATED: {
                break;
            }
        }
    }
}

