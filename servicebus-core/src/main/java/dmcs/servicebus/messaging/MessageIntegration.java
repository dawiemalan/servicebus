package dmcs.servicebus.messaging;

import dmcs.common.utils.Startable;
import dmcs.servicebus.address.EndpointAddress;

public abstract class MessageIntegration implements Startable {

    protected final MessageRouter messageRouter;
    protected final EndpointAddress endpointAddress;

    protected MessageIntegration(MessageRouter messageRouter, EndpointAddress endpointAddress) {
        this.messageRouter = messageRouter;
        this.endpointAddress = endpointAddress;
    }

    /**
     * Sends an outgoing message on the service bus
     */
    public abstract void send(EsbMessage message);

    public String getProtocol() {
        return endpointAddress.getProtocol();
    }
}
