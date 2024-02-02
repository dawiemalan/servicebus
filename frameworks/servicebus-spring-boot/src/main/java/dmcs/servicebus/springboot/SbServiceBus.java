package dmcs.servicebus.springboot;

import dmcs.servicebus.AbstractServiceBus;
import dmcs.servicebus.PlatformSupport;
import dmcs.servicebus.cluster.ClusterManager;
import dmcs.servicebus.config.ServiceBusProperties;
import dmcs.servicebus.services.ServiceManager;

/**
 * Spring boot service bus implementation
 */
public class SbServiceBus extends AbstractServiceBus {

    public SbServiceBus(ServiceBusProperties config, PlatformSupport platformSupport, ClusterManager clusterManager, ServiceManager serviceManager) {
        super(config, platformSupport, clusterManager, serviceManager);
    }

}
