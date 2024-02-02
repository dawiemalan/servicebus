package dmcs.servicebus.events;

import dmcs.servicebus.messaging.EsbMessage;

public class EsbMessageSentEvent extends EsbMessageEvent {
    public EsbMessageSentEvent(EsbMessage message) {
        super(message);
    }
}
