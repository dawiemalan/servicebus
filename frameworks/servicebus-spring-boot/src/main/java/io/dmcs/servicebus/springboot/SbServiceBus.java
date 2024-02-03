package io.dmcs.servicebus.springboot;

import io.dmcs.servicebus.AbstractServiceBus;
import io.dmcs.servicebus.PlatformSupport;
import io.dmcs.servicebus.cluster.ClusterManager;
import io.dmcs.servicebus.config.ServiceBusProperties;
import io.dmcs.servicebus.services.ServiceManager;

/**
 * Spring boot service bus implementation
 */
public class SbServiceBus extends AbstractServiceBus {

    public SbServiceBus(ServiceBusProperties config, PlatformSupport platformSupport, ClusterManager clusterManager, ServiceManager serviceManager) {
        super(config, platformSupport, clusterManager, serviceManager);
    }

}
