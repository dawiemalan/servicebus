package dmcs.servicebus.cluster.impl.websocket;

import dmcs.servicebus.PlatformSupport;
import dmcs.servicebus.cluster.ClusterManager;
import dmcs.servicebus.cluster.ClusterStateListener;
import dmcs.servicebus.services.AbstractServiceManager;
import dmcs.servicebus.services.ServiceManager;
import dmcs.servicebus.services.ServiceRegistration;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Collections;

@Context
@Requires(bean = WsClusterManager.class, missingBeans = {ServiceManager.class})
@Slf4j
public class WsServiceManager extends AbstractServiceManager implements ClusterStateListener {

    public WsServiceManager(@NotNull ClusterManager clusterManager,
                            @NotNull PlatformSupport platformSupport) {

        super(clusterManager, platformSupport);
        clusterManager.addClusterStateListener(this);
    }

    public synchronized void close() {

        if (clusterManager == null)
            return;

        clusterManager.removeClusterStateListener(this);
        clusterManager = null;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void clusterStateChanged(ClusterManager clusterManager, boolean connected) {

        this.clusterManager = clusterManager;

        if (connected)
            registerSelf();
    }

    @Override
    protected void registerSelf() {

    }

    @Override
    @SneakyThrows
    public Collection<ServiceRegistration> listServices() {
        return Collections.emptyList();
    }
}
