package io.dmcs.servicebus.events;

import io.dmcs.servicebus.messaging.EsbMessage;
import lombok.Getter;

@Getter
public class EsbMessageEvent extends EsbEvent {

    protected EsbMessage message;

    public EsbMessageEvent(EsbMessage message) {
        super();
        this.message = message;
    }
}
