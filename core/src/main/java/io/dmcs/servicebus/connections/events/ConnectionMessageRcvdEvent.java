package io.dmcs.servicebus.connections.events;

import io.dmcs.servicebus.connections.Connection;
import io.dmcs.servicebus.connections.ConnectionManager;
import lombok.Getter;
import lombok.ToString;

@ToString
public class ConnectionMessageRcvdEvent<C extends Connection<C, M>, M> extends ConnectionEvent<C, M> {

    @Getter
    private final M message;

    public ConnectionMessageRcvdEvent(ConnectionManager<C, M> manager, C connection, M message) {
        super(manager, connection);
        this.message = message;
    }
}
