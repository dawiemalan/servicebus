package dmcs.servicebus.connections;

import dmcs.common.listeners.Listenable;
import dmcs.servicebus.connections.exceptions.ConnectionException;
import io.micronaut.core.attr.MutableAttributeHolder;

import java.io.Closeable;
import java.io.IOException;

@SuppressWarnings("rawtypes")
public interface Connection<C extends Connection, M>
        extends AutoCloseable, Closeable, Listenable<ConnectionListener<C, M>>, MutableAttributeHolder {

    String getId();

    void setId(String id);

    void connect();

    void close() throws IOException;

    ConnectionStatus getStatus();

    void setStatus(ConnectionStatus status);

    default boolean isAutoReconnect() {
        return true;
    }

    /**
     * Implementation-specific function to send a message. Implementors must call
     * {@link AbstractConnection#fireMessageSent} before returning.
     *
     * @param message message to send
     * @throws ConnectionException
     */
    void sendMessage(M message) throws ConnectionException;
}
