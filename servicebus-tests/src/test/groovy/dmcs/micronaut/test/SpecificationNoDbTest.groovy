package dmcs.micronaut.test


import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Title

@Title('Specification with Artemis disabled and no database')
@MicronautTest
class SpecificationNoDbTest extends ServiceBusSpecification {

    @Override
    DatabaseType getDatabaseType() {
        return DatabaseType.None
    }

    @Override
    boolean enableArtemis() {
        return false
    }

    void 'test app start'() {
        when:
        def ctx = applicationContext

        then:
        ctx
        ctx.isRunning()
    }
}
