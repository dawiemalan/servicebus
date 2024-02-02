package dmcs.servicebus.micronaut;

import dmcs.servicebus.AbstractServiceBus;
import dmcs.servicebus.PlatformSupport;
import dmcs.servicebus.cluster.ClusterManager;
import dmcs.servicebus.config.ServiceBusProperties;
import dmcs.servicebus.services.ServiceManager;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.runtime.context.scope.Refreshable;
import io.micronaut.runtime.event.AbstractEmbeddedApplicationEvent;
import io.micronaut.runtime.server.event.ServerShutdownEvent;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

/**
 * Micronaut service bus implementation
 */
@Singleton
@Requires(bean = ServiceBusProperties.class)
@Refreshable("servicebus")
@Slf4j
public class MnServiceBus extends AbstractServiceBus implements ApplicationEventListener<AbstractEmbeddedApplicationEvent> {

    private boolean started = false;

    public MnServiceBus(ServiceBusProperties config, PlatformSupport platformSupport, ClusterManager clusterManager, ServiceManager serviceManager) {
        super(config, platformSupport, clusterManager, serviceManager);
    }

    @Override
    public synchronized void onApplicationEvent(AbstractEmbeddedApplicationEvent event) {

        if (event instanceof ServerStartupEvent && !started) {
            start();
            started = true;
        } else if (event instanceof ServerShutdownEvent && started) {
            stop();
            started = false;
        }
    }
}
