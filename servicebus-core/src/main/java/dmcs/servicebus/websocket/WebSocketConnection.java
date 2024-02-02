package dmcs.servicebus.websocket;

import dmcs.servicebus.connections.AbstractConnection;
import dmcs.servicebus.connections.ConnectionStatus;
import dmcs.servicebus.messaging.EsbMessage;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.websocket.CloseReason;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnError;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.net.URI;

@Slf4j
public abstract class WebSocketConnection extends AbstractConnection<WebSocketConnection, EsbMessage> {

    private final URI uri;
    private WebSocketSession session;

    public WebSocketConnection(@NonNull URI uri) {
        this.uri = uri;
    }

    public void close(CloseReason reason) {
        session.close(reason);
        fireClosed();
    }

    @OnOpen
    public void onOpen(WebSocketSession session) {
        this.session = session;
        setStatus(ConnectionStatus.CONNECTED);
    }

    @OnClose
    public void onClose(CloseReason reason) {

        if (reason == CloseReason.NORMAL)
            setStatus(ConnectionStatus.CLOSED);
        else
            setStatus(ConnectionStatus.DISCONNECTED);
    }

    @OnError
    public void onError(WebSocketSession session, RuntimeException e) {
        log.error("{} error: {}", session.getRequestURI(), e.getMessage());
        fireErrorOccurred(e);
    }

    @OnMessage
    public void onMessage(EsbMessage message) {
        fireMessageReceived(message);
    }

    public abstract void send(@NonNull @NotBlank EsbMessage message);

    @Override
    public void close() throws IOException {

        if (session != null)
            session.close(CloseReason.NORMAL);
        session = null;
        setStatus(ConnectionStatus.CLOSED);
    }
}
