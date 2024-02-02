package dmcs.servicebus.websocket.server;

import dmcs.servicebus.connections.AbstractConnection;
import dmcs.servicebus.connections.ConnectionStatus;
import dmcs.servicebus.connections.exceptions.ConnectionException;
import io.micronaut.websocket.CloseReason;
import io.micronaut.websocket.WebSocketSession;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public abstract class WebSocketServerConnection<C extends WebSocketServerConnection<C, M>, M> extends AbstractConnection<C, M> {

    protected WebSocketSession session;
    protected WebSocketServer<C, M> server;

    protected WebSocketServerConnection(WebSocketServer<C, M> server) {
        this.server = server;
    }

    @Override
    public void connect() {
        throw new IllegalArgumentException("Not implemented");
    }

    public void close(CloseReason reason) {
        session.close(reason);
        server.closed((C) this);
    }

    protected void onOpen(WebSocketSession session) {
        this.session = session;
        setStatus(ConnectionStatus.CONNECTED);
        server.connected((C) this);
    }

    protected void onClose(CloseReason reason) {

        if (reason == CloseReason.NORMAL) {
            setStatus(ConnectionStatus.CLOSED);
            return;
        }

        setStatus(ConnectionStatus.DISCONNECTED);
        reconnect();
    }

    protected void onError(WebSocketSession session, RuntimeException e) {
        log.error(String.format("%s error: %s", session.getRequestURI(), e.getMessage()), e);
        fireErrorOccurred(e);
    }

    protected void onMessage(M message) {
        fireMessageReceived(message);
    }

    @Override
    public void close() throws IOException {

        if (session != null)
            session.close(CloseReason.NORMAL);
        session = null;
        setStatus(ConnectionStatus.CLOSED);
    }

    @Override
    public void sendMessage(M message) throws ConnectionException {
        session.send(message);
    }
}
