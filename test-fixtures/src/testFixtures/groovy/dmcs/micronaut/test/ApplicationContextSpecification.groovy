package dmcs.micronaut.test

import io.micronaut.context.ApplicationContext
import io.micronaut.core.util.CollectionUtils
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.HttpClient
import io.micronaut.runtime.Micronaut
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

abstract class ApplicationContextSpecification extends Specification {

    @AutoCleanup
    @Shared
    EmbeddedServer embeddedServer

    @AutoCleanup
    @Shared
    ApplicationContext applicationContext

    @AutoCleanup
    @Shared
    HttpClient httpClient

    BlockingHttpClient getClient() {
        httpClient.toBlocking()
    }

    @Shared
    private Map<String, Object> defaultConfig

    abstract Class getMainClass()

    /**
     * Override this to customize configuration
     */
    void configure() {

    }

    String[] additionalEnvironments() {
        return []
    }

    void setConfig(String serviceName, String region, String environment) {

        addConfigProperties(
                'servicebus.service-name', serviceName.toLowerCase(),
                'servicebus.region', region.toLowerCase(),
                'servicebus.env', environment.toLowerCase()
        )
    }

    final void setupSpec() {

        defaultConfig = CollectionUtils.mapOf(
                'micronaut.server.port', '0',
                'micronaut.security.enabled', 'false',
                'micronaut.metrics.export.influx.enabled', 'false',
                'servicebus.service-name', 'testing',
                'servicebus.region', 'za',
                'servicebus.env', 'TEST'
        )

        // give implementations opportunity to customize configuration
        configure()

        List<String> environments = ['test']
        environments.addAll(additionalEnvironments())

        // start micronaut
        applicationContext = Micronaut.build()
                .packages("dmcs")
                .environments(environments.toArray() as String[])
                .properties(configuration)
                .banner(false)
                .mainClass(mainClass)
                .start()

        embeddedServer = applicationContext.findBean(EmbeddedServer).orElse(null)
        if (embeddedServer)
            httpClient = applicationContext.createBean(HttpClient, embeddedServer.URL)
    }

    /**
     * Add or override configuration properties
     * @param values key/value pairs
     */
    protected Map<String, Object> addConfigProperties(Object... values) {

        int len = values.length
        if (len % 2 != 0)
            throw new IllegalArgumentException("Number of arguments should be an even number representing the keys and values")

        int i = 0
        while (i < values.length - 1) {
            //noinspection GroovyAssignabilityCheck
            defaultConfig.put(values[i++], values[i++])
        }
    }

    protected Map<String, Object> getConfiguration() {
        return defaultConfig
    }

}
