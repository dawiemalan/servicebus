package dmcs.micronaut.test

import dmcs.TestApplication
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(application = TestApplication)
class ClientsSpec extends Specification implements TestPropertyProvider {

    @Inject
    TestClient testClient

    void testClients() {

        when:
        def value = testClient.greeting()

        then:
        value == 'hello'
    }

    @Override
    Map<String, String> getProperties() {
        return [
                'micronaut.server.port'     : '20000',
                'micronaut.security.enabled': 'false',
                'micronaut.metrics.enabled' : 'false'
        ]
    }
}
