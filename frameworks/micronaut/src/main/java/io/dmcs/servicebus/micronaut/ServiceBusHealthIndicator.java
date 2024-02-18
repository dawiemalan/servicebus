package io.dmcs.servicebus.micronaut;

import io.micronaut.context.annotation.Requires;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.health.indicator.AbstractHealthIndicator;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.annotation.Readiness;
import io.micronaut.runtime.context.scope.Refreshable;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.util.Map;

@Singleton
@Requires(classes = {HealthIndicator.class}, beans = {MnServiceBus.class})
@Requires(property = "servicebus.enabled", value = "true")
@Refreshable("servicebus")
@Readiness
@Slf4j
class ServiceBusHealthIndicator extends AbstractHealthIndicator<Map<String, String>> {

    private final MnServiceBus serviceBus;
    private StopWatch downTime;

    ServiceBusHealthIndicator(MnServiceBus serviceBus) {
        this.serviceBus = serviceBus;
    }

    @Override
    protected String getName() {
        return "servicebus";
    }

    @Override
    protected Map<String, String> getHealthInformation() {

        if (serviceBus.getClusterManager().isConnected()) {

            healthStatus = HealthStatus.UP;
            if (downTime != null)
                log.info("Reconnected to cluster after {}", downTime.formatTime());

            downTime = null;
            return Map.of("status", "Running");
        }

        String message = "Cluster disconnected";

        if (downTime == null) {
            downTime = StopWatch.createStarted();
        } else {
            message = String.format("%s for %s", message, downTime.formatTime());
            log.error(message);
        }

        healthStatus = new HealthStatus("DOWN", message, true, 100);
        return Map.of("status", message);
    }
}
