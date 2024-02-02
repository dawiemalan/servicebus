package dmcs.servicebus.micronaut;

import com.fasterxml.jackson.databind.ObjectMapper;
import dmcs.servicebus.PlatformSupport;
import dmcs.servicebus.cluster.ClusterManager;
import dmcs.servicebus.cluster.impl.websocket.WsClusterManager;
import dmcs.servicebus.cluster.impl.websocket.WsServiceManager;
import dmcs.servicebus.cluster.impl.zookeeper.ZkClusterManager;
import dmcs.servicebus.cluster.impl.zookeeper.ZkServiceManager;
import dmcs.servicebus.config.ClusterType;
import dmcs.servicebus.config.ServiceBusProperties;
import dmcs.servicebus.services.ServiceManager;
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

    @Context
    @Refreshable("servicebus")
    PlatformSupport platformSupport(@NotNull ApplicationContext context, @NotNull ServiceBusProperties serviceBusProperties, @NotNull Environment environment) {
        return new MnPlatformSupport(context, serviceBusProperties, environment);
    }

    @Bean
    Codec redissonCodec(@NotNull ObjectMapper objectMapper) {
        return new JsonJacksonCodec(objectMapper);
    }

//	@Context
//	@Bean(preDestroy = "stop")
//	@Requires(property = "servicebus.cluster-type", value = "redis")
//	@SneakyThrows
//	ClusterManager redisClusterManager(@NotNull ServiceBusProperties config, @NotNull PlatformSupport platformSupport,
//									   @Named(TaskExecutors.IO) ExecutorService executorService,
//									   @NotNull Codec codec) {
//
//		var redissonConfig = new Config();
//
//		if (config.getServerUrls().isEmpty())
//			throw new ExceptionInInitializerError("No server urls configured");
//
//		redissonConfig.useSingleServer().setAddress(config.getServerUrls().stream().findFirst().orElseThrow());
//		redissonConfig.setLockWatchdogTimeout(15000);
//		redissonConfig.setMinCleanUpDelay(5000);
//		redissonConfig.setMaxCleanUpDelay(30000);
//		redissonConfig.setExecutor(executorService);
//		redissonConfig.setReliableTopicWatchdogTimeout(300000);
//		redissonConfig.setReferenceEnabled(true);
//		redissonConfig.setCodec(codec);
//
//		var connectionListener = new RedissonConnectionListener();
//		redissonConfig.setConnectionListener(connectionListener);
//
//		RedissonClient redissonClient = Redisson.create(redissonConfig);
//
//		var cm = new RedisClusterManager(config, platformSupport, redissonClient);
//		connectionListener.setClusterManager(cm);
//
//		return cm;
//	}

    @Context
    @Bean(preDestroy = "stop")
    @Requires(property = "servicebus.cluster-type", notEquals = "redis")
    ClusterManager clusterManager(@NotNull ServiceBusProperties config, @NotNull PlatformSupport platformSupport) {

        if (config.getClusterType() == ClusterType.WEBSOCKET)
            return new WsClusterManager(config, platformSupport);
        else if (config.getClusterType() == ClusterType.ZOOKEEPER)
            return new ZkClusterManager(config, platformSupport);

        throw new ExceptionInInitializerError("Undefined cluster type: " + config.getClusterType());
    }

    @Context
    ServiceManager serviceManager(@NotNull ServiceBusProperties config, @NotNull ClusterManager clusterManager, @NotNull PlatformSupport platformSupport) {

        if (config.getClusterType() == ClusterType.WEBSOCKET)
            return new WsServiceManager(clusterManager, platformSupport);
        else if (config.getClusterType() == ClusterType.ZOOKEEPER)
            return new ZkServiceManager(clusterManager, platformSupport);

        throw new ExceptionInInitializerError("Undefined cluster type: " + config.getClusterType());
    }
}
