package io.dmcs.servicebus.cluster.impl.redis;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.connection.ConnectionListener;

import java.net.InetSocketAddress;

@Slf4j
public class RedissonConnectionListener implements ConnectionListener {

    @Getter
    @Setter
    protected RedisClusterManager clusterManager;

    public RedissonConnectionListener() {

    }

    @Override
    public void onConnect(InetSocketAddress addr) {
        log.debug("{} connected", addr.toString());
        if (clusterManager != null)
            clusterManager.notifyClusterStateChanged(true);
    }

    @Override
    public void onDisconnect(InetSocketAddress addr) {
        log.debug("{} disconnected", addr.toString());
        if (clusterManager != null)
            clusterManager.notifyClusterStateChanged(true);
    }
}
