package io.dmcs.servicebus;

import io.dmcs.servicebus.services.ServiceRegistration;

public interface ServiceBusListener {

    void serviceRegistered(ServiceRegistration serviceRegistration);
}
