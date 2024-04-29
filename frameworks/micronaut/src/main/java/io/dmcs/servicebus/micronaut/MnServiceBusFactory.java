package io.dmcs.servicebus.micronaut;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dmcs.servicebus.PlatformSupport;
import io.dmcs.servicebus.cluster.ClusterManager;
import io.dmcs.servicebus.cluster.impl.redis.RedisClusterManager;
import io.dmcs.servicebus.cluster.impl.redis.RedisServiceManager;
import io.dmcs.servicebus.cluster.impl.zookeeper.ZkClusterManager;
import io.dmcs.servicebus.cluster.impl.zookeeper.ZkServiceManager;
import io.dmcs.servicebus.config.ClusterType;
import io.dmcs.servicebus.config.ServiceBusProperties;
import io.dmcs.servicebus.micronaut.cluster.impl.websocket.WsClusterManager;
import io.dmcs.servicebus.micronaut.cluster.impl.websocket.WsServiceManager;
import io.dmcs.servicebus.services.ServiceManager;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.runtime.context.scope.Refreshable;
import jakarta.validation.constraints.NotNull;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;

@Requires(property = "servicebus.enabled", value = "true")
@Factory
public class MnServiceBusFactory {

    @SuppressWarnings("MnInjectionPoints")
    @Context
    @Refreshable("servicebus")
    PlatformSupport platformSupport(@NotNull ApplicationContext context, @NotNull ServiceBusProperties serviceBusProperties,
                                    @NotNull Environment environment) {
        return new MnPlatformSupport(context, serviceBusProperties, environment);
    }

    @SuppressWarnings("MnInjectionPoints")
    @Bean
    Codec redissonCodec(@NotNull ObjectMapper objectMapper) {
        return new JsonJacksonCodec(objectMapper);
    }

    @Context
    @Bean(preDestroy = "stop")
    @Requires(property = "servicebus.cluster-type", notEquals = "redis")
    ClusterManager clusterManager(@NotNull ServiceBusProperties config, @NotNull PlatformSupport platformSupport) {

        if (config.getClusterType() == ClusterType.WEBSOCKET)
            return new WsClusterManager(config, platformSupport);
        else if (config.getClusterType() == ClusterType.ZOOKEEPER)
            return new ZkClusterManager(config, platformSupport);
        else if (config.getClusterType() == ClusterType.REDIS)
            return new RedisClusterManager(config, platformSupport);

        throw new ExceptionInInitializerError("Undefined cluster type: " + config.getClusterType());
    }

    @Context
    ServiceManager serviceManager(@NotNull ServiceBusProperties config, @NotNull ClusterManager clusterManager,
                                  @NotNull PlatformSupport platformSupport) {

        if (config.getClusterType() == ClusterType.WEBSOCKET)
            return new WsServiceManager(clusterManager, platformSupport);
        else if (config.getClusterType() == ClusterType.ZOOKEEPER)
            return new ZkServiceManager(clusterManager, platformSupport);
        else if (config.getClusterType() == ClusterType.REDIS)
            return new RedisServiceManager(clusterManager, platformSupport);

        throw new ExceptionInInitializerError("Undefined cluster type: " + config.getClusterType());
    }
}
