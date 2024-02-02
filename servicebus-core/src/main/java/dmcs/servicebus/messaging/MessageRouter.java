package dmcs.servicebus.messaging;

import dmcs.common.utils.Startable;

public interface MessageRouter extends Startable {

    /**
     * Sends an outgoing message
     */
    void sendMessage(EsbMessage message);

    /**
     * Process an incoming message
     */
    void onMessage(EsbMessage message);
}
