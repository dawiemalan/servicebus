package io.dmcs.servicebus.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.time.Duration;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@ToString
public class MessageOptions implements Serializable {

    static final long serialVersionUID = 1L;

    static final MessageOptions DEFAULT = MessageOptions.builder().build();

    private int priority;

    /**
     * Maximum time to live in seconds (default 24 hours)
     */
    @Builder.Default
    private long timeToLiveSecs = Duration.ofHours(24).toMillis();

    /**
     * Initial time between retries. Will increase exponentially with each retry (default 10 seconds)
     */
    @Builder.Default
    long retryInterval = 10000L;

    /**
     * Maximum number of retries (default -1, i.e. no limit)
     */
    @Builder.Default
    int maxRetries = -1;

    /**
     * Maximum retry interval (default 5 minutes)
     */
    @Builder.Default
    long maxRetryInterval = 300000L;

    /**
     * If the message is not persistent it will be treated as a topic message
     */
    @Builder.Default
    boolean persistent = true;

}
