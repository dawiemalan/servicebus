package io.dmcs.servicebus.connections;

public interface ConnectionListener<C extends Connection, MSG_TYPE> {

    void connected(C connection);

    void closed(C connection);

    void messageReceived(C connection, MSG_TYPE message);

    void messageSent(C connection, MSG_TYPE message);

    void statusChanged(C connection, ConnectionStatus before, ConnectionStatus after);

    void errorOccurred(C connection, RuntimeException e);
}
