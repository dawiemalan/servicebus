package dmcs.servicebus.events;

import dmcs.servicebus.messaging.EsbMessage;

public class EsbMessageRcvdEvent extends EsbMessageEvent {
    public EsbMessageRcvdEvent(EsbMessage message) {
        super(message);
    }
}
