package dmcs.servicebus.address


import dmcs.servicebus.exceptions.InvalidAddressException
import dmcs.servicebus.services.ServiceRegistration
import spock.lang.Specification

class AddressSpec extends Specification {

    void "endpoint address parsing"() {
        when:
        def address = EndpointAddress.of("http://localhost:10/ctx/1")

        then:
        address.port == 10
        address.host == 'localhost'
        address.protocol == 'http'
        address.suffix == "ctx/1"
        address.toString() == "http://localhost:10/ctx/1"

        when:
        address = EndpointAddress.of("http://localhost/ctx/1")

        then:
        address.port == 0
        address.host == 'localhost'
        address.protocol == 'http'
        address.suffix == "ctx/1"
        address.toString() == "http://localhost/ctx/1"

        when:
        address = EndpointAddress.of("http://localhost")

        then:
        address.port == 0
        address.host == 'localhost'
        address.protocol == 'http'
        !address.suffix
        address.toString() == "http://localhost"
        //noinspection GrEqualsBetweenInconvertibleTypes
        address == "http://localhost"
        //noinspection GrEqualsBetweenInconvertibleTypes
        address != "https://localhost"
    }

    void "service address parsing"() {

        when:
        ServiceAddress.of("http://localhost")

        then:
        thrown(InvalidAddressException)

        when:
        def address = ServiceAddress.of("servicebus@global.test:d2dab43086")

        then:
        address.name == 'servicebus'
        address.stackId == 'global'
        address.environment == 'test'
        address.instanceId == 'd2dab43086'
        address.hashCode() == "servicebus@global.test:d2dab43086".hashCode()
        address.serviceGroup == "servicebus@global.test"

        when:
        address = ServiceAddress.of("servicebus@global.test")

        then:
        address.toString() == "servicebus@global.test"
        address.serviceGroup == "servicebus@global.test"

        when:
        address = ServiceAddress.builder().name('servicebus').build()

        then:
        address.name == 'servicebus'
        !address.stackId
        !address.environment
        !address.instanceId
        address.toString() == "servicebus"
        address.serviceGroup == "servicebus"

        when:
        address = ServiceAddress.of("servicebus@global.test")

        then:
        address.toString() == "servicebus@global.test"
        address.serviceGroup == "servicebus@global.test"
    }

    void "service address matching"() {

        when:
        def address = ServiceAddress.of("servicebus@global.test:d2dab43086")
        def registration = ServiceRegistration.builder()
                .name(address.getName())
                .stackId(address.getStackId())
                .environment(address.getEnvironment())
                .instanceId(address.getInstanceId())
                .build()

        then:
        registration.matches("*")
        registration.matches("servicebus")
        registration.matches("servicebus*")
        registration.matches("servicebus@*")
        registration.matches("servicebus@global.test:d2dab43086")
        registration.matches("servicebus@global.test")
        registration.matches(" servicebus@global.test ")
        registration.matches("*@global.test")
        !registration.matches("*@test")
        registration.matches(ServiceLookupQuery.builder().name("servicebus").build())
        registration.matches(ServiceLookupQuery.builder().name("servicebus").instanceId("d2dab43086").build())
        !registration.matches(ServiceLookupQuery.builder().name("servicebus").instanceId("d2dab43088").build())
        registration.matches(ServiceLookupQuery.builder().instanceId("d2dab43086").build())
    }
}
