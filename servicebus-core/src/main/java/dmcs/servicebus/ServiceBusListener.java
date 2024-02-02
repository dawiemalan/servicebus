package dmcs.servicebus;

import dmcs.servicebus.services.ServiceRegistration;

public interface ServiceBusListener {

    void serviceRegistered(ServiceRegistration serviceRegistration);
}
