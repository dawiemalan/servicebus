package io.dmcs.servicebus.events;

import io.dmcs.servicebus.messaging.EsbMessage;
import io.micronaut.core.annotation.Introspected;
import lombok.Getter;

@Getter
@Introspected
public class EsbMessageEvent extends EsbEvent {

    protected EsbMessage message;

    public EsbMessageEvent(EsbMessage message) {
        super();
        this.message = message;
    }
}
