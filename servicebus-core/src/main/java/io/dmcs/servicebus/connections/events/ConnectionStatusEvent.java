package io.dmcs.servicebus.connections.events;

import io.dmcs.servicebus.connections.Connection;
import io.dmcs.servicebus.connections.ConnectionManager;
import io.dmcs.servicebus.connections.ConnectionStatus;
import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.ToString;

@Introspected
@ToString
public class ConnectionStatusEvent<C extends Connection<C, M>, M> extends ConnectionEvent<C, M> {

    @Getter
    private final ConnectionStatus before;
    @Getter
    private final ConnectionStatus after;

    public ConnectionStatusEvent(ConnectionManager<C, M> manager, C connection, ConnectionStatus before, ConnectionStatus after) {
        super(manager, connection);
        this.before = before;
        this.after = after;
    }
}
