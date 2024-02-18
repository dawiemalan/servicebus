package io.dmcs.servicebus.test;

import io.dmcs.servicebus.address.EndpointAddress;
import io.dmcs.servicebus.address.ServiceAddress;
import io.dmcs.servicebus.address.ServiceLookupQuery;
import io.dmcs.servicebus.exceptions.InvalidAddressException;
import io.dmcs.servicebus.services.ServiceRegistration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AddressTest {

	@Test
	void endpointAddressParsing() throws Exception {

		var address = EndpointAddress.of("http://localhost:10/ctx/1");

		Assertions.assertEquals(10, address.getPort());
		Assertions.assertEquals("localhost", address.getHost());
		Assertions.assertEquals("http", address.getProtocol());
		Assertions.assertEquals("ctx/1", address.getSuffix());
		Assertions.assertEquals("http://localhost:10/ctx/1", address.toString());

		address = EndpointAddress.of("http://localhost/ctx/1");

		Assertions.assertEquals(0, address.getPort());
		Assertions.assertEquals("localhost", address.getHost());
		Assertions.assertEquals("http", address.getProtocol());
		Assertions.assertEquals("ctx/1", address.getSuffix());
		Assertions.assertEquals("http://localhost/ctx/1", address.toString());

		address = EndpointAddress.of("http://localhost");

		Assertions.assertEquals(0, address.getPort());
		Assertions.assertEquals("localhost", address.getHost());
		Assertions.assertEquals("http", address.getProtocol());
		Assertions.assertNull(address.getSuffix());
		Assertions.assertEquals("http://localhost", address.toString());
	}

	@Test
	void serviceAddressParsing() throws Exception {

		Assertions.assertThrows(InvalidAddressException.class, () -> ServiceAddress.of("http://localhost"));

		var address = ServiceAddress.of("servicebus@global.test:d2dab43086");

		Assertions.assertEquals("servicebus", address.getName());
		Assertions.assertEquals("global", address.getRegion());
		Assertions.assertEquals("test", address.getEnvironment());
		Assertions.assertEquals("d2dab43086", address.getInstanceId());
		Assertions.assertEquals("servicebus@global.test:d2dab43086".hashCode(), address.hashCode());
		Assertions.assertEquals("servicebus@global.test", address.getServiceGroup());

		address = ServiceAddress.of("servicebus@global.test");

		Assertions.assertEquals("servicebus@global.test", address.toString());
		Assertions.assertEquals("servicebus@global.test", address.getServiceGroup());

		address = ServiceAddress.builder().name("servicebus").build();

		Assertions.assertEquals("servicebus", address.getName());
		Assertions.assertNull(address.getRegion());
		Assertions.assertNull(address.getEnvironment());
		Assertions.assertNull(address.getInstanceId());
		Assertions.assertEquals("servicebus", address.toString());
		Assertions.assertEquals("servicebus", address.getServiceGroup());

		address = ServiceAddress.of("servicebus@global.test");

		Assertions.assertEquals("servicebus@global.test", address.toString());
		Assertions.assertEquals("servicebus@global.test", address.getServiceGroup());
	}

	@Test
	void serviceAddressMatching() throws Exception {

		var address = ServiceAddress.of("servicebus@global.test:d2dab43086");
		var registration = ServiceRegistration.builder()
				.name(address.getName())
				.region(address.getRegion())
				.environment(address.getEnvironment())
				.instanceId(address.getInstanceId())
				.build();

		Assertions.assertTrue(registration.matches("*"));
		Assertions.assertTrue(registration.matches("servicebus"));
		Assertions.assertTrue(registration.matches("servicebus*"));
		Assertions.assertTrue(registration.matches("servicebus@*"));
		Assertions.assertTrue(registration.matches("servicebus@global.test:d2dab43086"));
		Assertions.assertTrue(registration.matches("servicebus@global.test"));
		Assertions.assertTrue(registration.matches(" servicebus@global.test "));
		Assertions.assertTrue(registration.matches("*@global.test"));
		Assertions.assertFalse(registration.matches("*@test"));
		Assertions.assertTrue(registration.matches(ServiceLookupQuery.builder().name("servicebus").build()));
		Assertions.assertTrue(registration.matches(ServiceLookupQuery.builder().name("servicebus").instanceId("d2dab43086").build()));
		Assertions.assertFalse(registration.matches(ServiceLookupQuery.builder().name("servicebus").instanceId("d2dab43088").build()));
		Assertions.assertTrue(registration.matches(ServiceLookupQuery.builder().instanceId("d2dab43086").build()));
	}
}
