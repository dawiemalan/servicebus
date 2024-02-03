package io.dmcs.servicebus.services.events;

import io.dmcs.servicebus.events.EsbEvent;
import io.dmcs.servicebus.services.ServiceRegistration;
import lombok.Getter;
import lombok.ToString;

@ToString(of = {"registration", "event"})
public class ServiceEvent extends EsbEvent {

    @Getter
    private ServiceRegistration registration;

    @Getter
    private final String event;

    public ServiceEvent(ServiceRegistration registration, String event) {

        super();
        this.registration = registration;
        this.event = event;
    }
}
