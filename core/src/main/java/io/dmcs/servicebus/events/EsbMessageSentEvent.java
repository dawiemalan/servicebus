package io.dmcs.servicebus.events;

import io.dmcs.servicebus.messaging.EsbMessage;

public class EsbMessageSentEvent extends EsbMessageEvent {
    public EsbMessageSentEvent(EsbMessage message) {
        super(message);
    }
}
