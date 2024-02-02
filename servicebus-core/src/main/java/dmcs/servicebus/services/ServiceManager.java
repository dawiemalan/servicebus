package dmcs.servicebus.services;

import dmcs.common.utils.Startable;
import dmcs.servicebus.address.ServiceLookupQuery;

import java.util.Collection;

public interface ServiceManager extends Startable {

    void start();

    void stop();

    /**
     * This service's registration
     */
    ServiceRegistration getServiceRegistration();

    /**
     * Lists all registered services
     */
    Collection<ServiceRegistration> listServices();

    /**
     * Lists all registered services in a group
     */
    Collection<ServiceRegistration> listServices(String serviceGroup);

    /**
     * Locate services
     */
    Collection<ServiceRegistration> locateServices(ServiceLookupQuery query);
}
