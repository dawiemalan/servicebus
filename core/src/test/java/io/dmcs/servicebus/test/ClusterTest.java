package io.dmcs.servicebus.test;

import io.dmcs.servicebus.cluster.impl.redis.RedisClusterManager;
import io.dmcs.servicebus.config.ServiceBusProperties;
import io.dmcs.test.TestApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@MicronautTest(application = TestApplication.class)
@Slf4j
class ClusterTest {

    @Inject
	ServiceBusProperties config;
//	@Inject
//	ClusterManager clusterManager
//	@Inject

	RedissonClient redissonClient;
	RedissonClient redissonClient1;
	RedissonClient redissonClient2;
	RedisClusterManager cluster1;
	RedisClusterManager cluster2;

	@BeforeEach
	void setup() {

		if (cluster1 == null) {

			redissonClient1 = Redisson.create(redissonClient.getConfig());
			cluster1 = startCluster("member1", redissonClient1);
			redissonClient2 = Redisson.create(redissonClient.getConfig());
			cluster2 = startCluster("member2", redissonClient2);
		}
    }

	@Test
	void simpleLockTests() {

		var lock = cluster1.getLock("locks/a_lock");
		lock.acquire();

		Assertions.assertTrue(lock.isAcquiredInThisProcess());

		// should not be able to obtain lock on another member
		AtomicBoolean locked = new AtomicBoolean(false);
		new Thread(() -> {
			var lock2 = cluster2.getLock("locks/a_lock");
			locked.set(lock2.acquire(100, TimeUnit.MILLISECONDS));
		}).start();

		Awaitility.waitAtMost(500, TimeUnit.MILLISECONDS).await();

		var lock2 = cluster2.getLock("/locks/a_lock");

		Assertions.assertFalse(locked.get());
		Assertions.assertFalse(lock2.isAcquiredInThisProcess());

		lock.release();

		Assertions.assertFalse(lock.isAcquiredInThisProcess());
	}
//
//	void "leadergroup tests"() {
//
//		when:
//		def group1 = cluster1.getLeaderGroup('testgroup')
//		Thread.sleep(500)
//		def group2 = cluster2.getLeaderGroup('testgroup')
//		Thread.sleep(500)
//
//		then:
//		group1.master
//		!group2.master
//
//	}

	private RedisClusterManager startCluster(String name, RedissonClient redisson) {

		log.info("Starting cluster {}", name);

		//var config = new TestClusterConfig(instanceName: name)
		return new RedisClusterManager(config, null, redissonClient);
	}

}
