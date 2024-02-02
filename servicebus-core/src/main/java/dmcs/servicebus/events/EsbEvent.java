package dmcs.servicebus.events;

import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

@Getter
public abstract class EsbEvent implements Serializable {

    static final long serialVersionUID = 1L;

    private final String eventId;

    protected EsbEvent() {
        this.eventId = UUID.randomUUID().toString();
    }

}
