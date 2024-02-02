package dmcs.servicebus;

import dmcs.common.utils.Startable;
import dmcs.servicebus.address.ServiceLookupQuery;
import dmcs.servicebus.cluster.ClusterManager;
import dmcs.servicebus.cluster.leadership.LeaderGroup;
import dmcs.servicebus.cluster.locks.DistributedLock;
import dmcs.servicebus.events.EsbEvent;
import dmcs.servicebus.messaging.EsbMessage;
import dmcs.servicebus.services.ServiceManager;
import dmcs.servicebus.services.ServiceRegistration;

import java.util.Collection;

/**
 * Base class for service bus implementations
 */
public interface ServiceBus extends Startable {

    ClusterManager getClusterManager();

    ServiceManager getServiceManager();

    /**
     * This service's registration info
     *
     * @return
     */
    ServiceRegistration getServiceRegistration();

    DistributedLock getLock(String lockName);

    LeaderGroup getLeaderGroup(String name);

    Collection<LeaderGroup> getLeaderGroups();

    /**
     * Initializes the service bus and cluster
     */
    void start();

    void stop();

    /**
     * Sends an outgoing message on the service bus
     */
    void publish(EsbMessage message);

    /**
     * Sends an outgoing event on the service bus
     */
    void publish(EsbEvent event);

    /**
     * Lists all registered services
     */
    Collection<ServiceRegistration> listServices();

    /**
     * Locate services
     */
    Collection<ServiceRegistration> locateServices(ServiceLookupQuery query);
}
