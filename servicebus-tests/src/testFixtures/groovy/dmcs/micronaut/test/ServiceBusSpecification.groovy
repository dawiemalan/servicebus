//file:noinspection unused
//file:noinspection GrMethodMayBeStatic
package dmcs.micronaut.test

import groovy.util.logging.Slf4j
import io.micronaut.context.ApplicationContext
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MariaDBContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Shared
import spock.lang.Specification

@Slf4j
abstract class ServiceBusSpecification extends Specification implements TestPropertyProvider, ServiceBusSpecificationConfig {

    static final String ARTEMIS_IMAGE = 'quay.io/artemiscloud/activemq-artemis-broker'
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
    protected static GenericContainer artemisContainer
    protected static MariaDBContainer mariaDbContainer
    protected static MySQLContainer mysqlDbContainer
    protected static PostgreSQLContainer postgresContainer
    protected static GenericContainer redisContainer
    protected static GenericContainer zooKeeperContainer
    protected static GenericContainer rabbitMqContainer

    String getDatabaseName() {
        return 'test'
    }

    @Override
    Map<String, String> getProperties() {

        startArtemisContainer()
        startDbContainer()
        startLdapContainer()
        startRedisContainer()
        startRabbitMqContainer()
        startZooKeeperContainer()

        def properties = [
                "micronaut.server.port"                  : "-1",
                "micronaut.metrics.export.influx.enabled": "false",
                "micronaut.jms.enabled"                  : "false",
                "micronaut.jms.activemq.artemis.enabled" : "false",
                'micronaut.jms.auto-start'               : 'false',
                'dataSource.dbCreate'                    : 'create-drop',
                'hibernate.hbm2ddl.auto'                 : 'create-drop',
                'dataSource.properties.initialSize'      : '5',
                'dataSource.properties.maximumPoolSize'  : '15',
                'micronaut.http.services.core.urls'      : "http://localhost",
                "micronaut.env.deduction"                : "false"
        ]

        if (enableArtemis())
            properties.putAll([
                    "micronaut.jms.enabled"                           : "true",
                    "micronaut.jms.activemq.artemis.enabled"          : "true",
                    "micronaut.jms.activemq.artemis.connection-string":
                            "tcp://${artemisContainer.host}:${artemisContainer.getMappedPort(61616)}?consumerWindowSize=0",
                    "micronaut.jms.activemq.artemis.username"         : "admin",
                    "micronaut.jms.activemq.artemis.password"         : "admin",
                    'micronaut.jms.auto-start'                        : 'false',
            ])

        if (enableRedis())
            properties.putAll([
                    "redis.uri": "redis://${redisContainer.getHost()}:${redisContainer.getMappedPort(6379)}",
            ])

        if (enableRabbitMq())
            properties.putAll([
//					"rabbitmq.host": rabbitMqContainer.host,
//					"rabbitmq.port": "${rabbitMqContainer.getMappedPort(5672)}"
"rabbitmq.host": 'localhost',
"rabbitmq.port": "5672"
            ])

        if (enableDatabase())
            return configureDatasource(properties)

        return properties
    }

    private void startArtemisContainer() {

        if (!enableArtemis())
            return

        if (!artemisContainer) {
            artemisContainer = new GenericContainer<>(ARTEMIS_IMAGE)
                    .withEnv([
                            "AMQ_USER"    : "admin",
                            "AMQ_PASSWORD": "admin",
                    ])
                    .withExposedPorts(8161, 61616, 5672)
        }

        if (!artemisContainer.running)
            artemisContainer.start()
    }

    void restartArtemis() {

        if (!enableArtemis() || !artemisContainer)
            return

        artemisContainer.stop()
        artemisContainer.start()
    }

    @SuppressWarnings('GroovyFallthrough')
    void restartDatabase() {

        switch (databaseType) {
            case DatabaseType.None:
            case DatabaseType.H2:
                // no container
                return
            case DatabaseType.MySQL:
                if (mysqlDbContainer) {
                    mysqlDbContainer.stop()
                    mysqlDbContainer.start()
                }
                break
            case DatabaseType.MariaDB:
                if (mariaDbContainer) {
                    mariaDbContainer.stop()
                    mariaDbContainer.start()
                }
                break
            case DatabaseType.Postgres:
                if (!postgresContainer) {
                    postgresContainer.stop()
                    postgresContainer.start()
                }
                break
        }

        log.info("Restarted database container: $databaseType")
    }

    private void startDbContainer() {

        if (!enableDatabase())
            return

        switch (databaseType) {
            case DatabaseType.None:
                break
            case DatabaseType.MySQL:
                if (!mysqlDbContainer)
                    mysqlDbContainer = new MySQLContainer<>('mysql:8').withDatabaseName(databaseName)
                if (!mysqlDbContainer.running)
                    mysqlDbContainer.start()
                break
            case DatabaseType.MariaDB:
                if (!mariaDbContainer)
                    mariaDbContainer = new MariaDBContainer<>('mariadb:10.7.3').withDatabaseName(databaseName)
                if (!mariaDbContainer.running)
                    mariaDbContainer.start()
                break
            case DatabaseType.Postgres:
                if (!postgresContainer)
                    postgresContainer = new PostgreSQLContainer<>('postgres:14.1-alpine').withDatabaseName(databaseName)
                if (!postgresContainer.running)
                    postgresContainer.start()
                break
            case DatabaseType.H2:
                // no container needed
                break
        }
    }

    private Map<String, String> configureDatasource(Map<String, String> properties) {

        switch (databaseType) {
            case DatabaseType.None:
                break
            case DatabaseType.MySQL:
                configureMySQL(properties)
                break
            case DatabaseType.MariaDB:
                configureMariaDb(properties)
                break
            case DatabaseType.H2:
                configureH2(properties)
                break
            case DatabaseType.Postgres:
                configurePostgres(properties)
                break
        }

        return properties
    }

    private Map<String, String> configureH2(Map<String, String> properties) {

        properties.putAll([
                'activiti.databaseType'     : 'h2',
                'dataSource.driverClassName': 'org.h2.Driver',
                'dataSource.url'            : 'jdbc:h2:mem:testDb;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=TRUE;IGNORECASE=TRUE',
        ])

        return properties
    }

    private Map<String, String> configureMySQL(Map<String, String> properties) {

        properties.putAll([
                'activiti.databaseType'     : 'mysql',
                'dataSource.driverClassName': 'com.mysql.cj.jdbc.Driver',
                'dataSource.url'            : mysqlDbContainer.jdbcUrl + '?TC_TMPFS=/testtmpfs:rw&TC_MY_CNF=mysql',
                'dataSource.username'       : mysqlDbContainer.username,
                'dataSource.password'       : mysqlDbContainer.password,
        ])

        return properties
    }

    private Map<String, String> configureMariaDb(Map<String, String> properties) {

        properties.putAll([
                'activiti.databaseType'              : 'mysql',
                'dataSource.driverClassName'         : 'org.mariadb.jdbc.Driver',
                'dataSource.url'                     : mariaDbContainer.jdbcUrl,
                'dataSource.username'                : mariaDbContainer.username,
                'dataSource.password'                : mariaDbContainer.password,
                'dataSources.default.driverClassName': 'org.mariadb.jdbc.Driver',
                'dataSources.default.url'            : mariaDbContainer.jdbcUrl,
                'dataSources.default.username'       : mariaDbContainer.username,
                'dataSources.default.password'       : mariaDbContainer.password,
        ])

        return properties
    }

    private Map<String, String> configurePostgres(Map<String, String> properties) {

        properties.putAll([
                'activiti.databaseType'     : 'postgres',
                'dataSource.driverClassName': 'org.postgresql.Driver',
                'dataSource.url'            : postgresContainer.jdbcUrl,
                'dataSource.username'       : postgresContainer.username,
                'dataSource.password'       : postgresContainer.password,
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

    static enum DatabaseType {
        None,
        H2,
        MariaDB,
        MySQL,
        Postgres
    }
}
