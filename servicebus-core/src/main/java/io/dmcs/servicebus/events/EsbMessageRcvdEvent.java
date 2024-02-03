package io.dmcs.servicebus.events;

import io.dmcs.servicebus.messaging.EsbMessage;

public class EsbMessageRcvdEvent extends EsbMessageEvent {
    public EsbMessageRcvdEvent(EsbMessage message) {
        super(message);
    }
}
