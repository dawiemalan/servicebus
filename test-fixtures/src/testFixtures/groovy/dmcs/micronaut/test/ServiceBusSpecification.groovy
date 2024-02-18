//file:noinspection unused
//file:noinspection GrMethodMayBeStatic
package dmcs.micronaut.test

import groovy.util.logging.Slf4j
import io.micronaut.context.ApplicationContext
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import org.testcontainers.containers.GenericContainer
import spock.lang.Shared
import spock.lang.Specification

@Slf4j
abstract class ServiceBusSpecification extends Specification implements TestPropertyProvider, ServiceBusSpecificationConfig {

    static final String ZOOKEEPER_IMAGE = 'bitnami/zookeeper'
    static final String RABBITMQ_IMAGE = 'rabbitmq:3-management-alpine'

    @Inject
    @Shared
    ApplicationContext applicationContext

    @Shared
    @Inject
    EmbeddedServer embeddedServer

    /**
     * containers - note: do not annotate with AutoCleanup to keep them running between specs
     */
    protected static GenericContainer redisContainer
    protected static GenericContainer zooKeeperContainer
    protected static GenericContainer rabbitMqContainer

    String getDatabaseName() {
        return 'test'
    }

    @Override
    Map<String, String> getProperties() {

        startRedisContainer()
        startRabbitMqContainer()
        startZooKeeperContainer()

        def properties = [
                "micronaut.server.port"                  : "-1",
                "micronaut.metrics.export.influx.enabled": "false",
//                'dataSource.dbCreate'                    : 'create-drop',
//                'hibernate.hbm2ddl.auto'                 : 'create-drop',
//                'dataSource.properties.initialSize'      : '5',
//                'dataSource.properties.maximumPoolSize'  : '15',
                'micronaut.http.services.core.urls'      : "http://localhost",
                "micronaut.env.deduction"                : "false"
        ]

        if (enableRedis())
            properties.putAll([
                    "redis.uri": "redis://${redisContainer.getHost()}:${redisContainer.getMappedPort(6379)}",
            ])

        if (enableRabbitMq())
            properties.putAll([
					"rabbitmq.host": rabbitMqContainer.host,
					"rabbitmq.port": "${rabbitMqContainer.getMappedPort(5672)}"
            ])

        return properties
    }


    private void startRedisContainer() {

        if (!enableRedis())
            return

        if (!redisContainer) {
            redisContainer = new GenericContainer<>("redis")
                    .withExposedPorts(6379)
        }

        if (!redisContainer.running)
            redisContainer.start()
    }

    private void startZooKeeperContainer() {

        if (!enableZooKeeper())
            return

        if (!zooKeeperContainer) {
            zooKeeperContainer = new GenericContainer<>(ZOOKEEPER_IMAGE)
                    .withEnv('ALLOW_ANONYMOUS_LOGIN', 'yes')
                    .withExposedPorts(2181)
        }

        if (!zooKeeperContainer.running)
            zooKeeperContainer.start()
    }

    private void startRabbitMqContainer() {

        if (!enableRabbitMq())
            return

        if (!rabbitMqContainer) {
            rabbitMqContainer = new GenericContainer<>(RABBITMQ_IMAGE)
                    .withExposedPorts(5672, 15672)
        }

        if (!rabbitMqContainer.running)
            rabbitMqContainer.start()
    }
}
