package dmcs.servicebus.cluster.impl.websocket;

import jakarta.validation.constraints.NotBlank;

public interface WsClusterClient extends AutoCloseable {

    void onMessage(String message);

    void onOpen();

    void onClose(String reason);

    void onError(Throwable error);

    void send(@NotBlank String message);

}
