package io.dmcs.servicebus.connections.events;

import io.dmcs.servicebus.connections.Connection;
import io.dmcs.servicebus.connections.ConnectionManager;
import io.dmcs.servicebus.events.EsbEvent;
import lombok.Getter;
import lombok.ToString;

@ToString
public class ConnectionEvent<C extends Connection<C, M>, M> extends EsbEvent {

    @Getter
    private final C connection;
    @Getter
    private final ConnectionManager<C, M> manager;

    protected ConnectionEvent(ConnectionManager<C, M> manager, C connection) {
        this.manager = manager;
        this.connection = connection;
    }
}
