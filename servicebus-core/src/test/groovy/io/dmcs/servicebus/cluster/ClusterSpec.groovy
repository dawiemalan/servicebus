package io.dmcs.servicebus.cluster

import dmcs.TestApplication
import dmcs.micronaut.test.ServiceBusSpecification
import groovy.util.logging.Slf4j
import io.dmcs.servicebus.config.ServiceBusProperties
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Ignore

@MicronautTest(application = TestApplication)
@Slf4j
@Ignore
class ClusterSpec extends ServiceBusSpecification {

    @Inject
    ServiceBusProperties config
//	@Inject
//	ClusterManager clusterManager
//	@Inject
//	@Shared
//	RedissonClient redissonClient
//	@Shared
//	RedissonClient redissonClient1
//	@Shared
//	RedissonClient redissonClient2
//
//	@Shared
//	RedisClusterManager cluster1
//	@Shared
//	RedisClusterManager cluster2

    void setupSpec() {

//		def m = hz.getReplicatedMap('test-map')
//
//		if (!cluster1) {
//
//			redissonClient1 = Redisson.create(redissonClient.config)
//			cluster1 = startCluster('member1', redissonClient1)
//			redissonClient2 = Redisson.create(redissonClient.config)
//			cluster2 = startCluster('member2', redissonClient2)
//		}
    }

    void test() {
        when:
        def hz = HazelcastClient.newHazelcastClient()

        then:
        true
    }

//	void "simple lock tests"() {
//
//		when:
//		def lock = cluster1.getLock('locks/a_lock')
//		lock.tryLock()
//
//		then:
//		lock.heldByCurrentThread
//
//		when: "should not be able to obtain lock on another member"
//		AtomicBoolean locked = new AtomicBoolean(false)
//		new Thread({
//			def lock2 = cluster2.getLock('locks/a_lock')
//			locked.set(lock2.tryLock(100, TimeUnit.MILLISECONDS))
//		}).run()
//		Thread.sleep(200)
//		def lock2 = cluster2.getLock('/locks/a_lock')
//
//		then:
//		!locked.get()
//		!lock2.heldByCurrentThread
//
//		when:
//		lock.unlock()
//
//		then:
//		!lock.heldByCurrentThread
//	}
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

    @Override
    boolean enableArtemis() {
        return false
    }

    @Override
    boolean enableDatabase() {
        return false
    }

    @Override
    boolean enableZooKeeper() {
        return true
    }

//	private RedisClusterManager startCluster(String name, RedissonClient redisson) {
//
//		log.info("Starting cluster $name")
//
//		def config = new TestClusterConfig(instanceName: name)
//		return new RedisClusterManager(redisson, null, config)
//	}
//
//	static class TestClusterConfig implements ClusterConfigProperties {
//
//		String instanceName
//
//		@Override
//		Boolean isEnabled() { return true }
//
//		@Override
//		String getInstanceName() {
//			return instanceName
//		}
//
//		@Override
//		RedisConfigProperties getRedis() {
//			return new RedisConfigProperties() {
//				@Override
//				Boolean isEnabled() { return true }
//			}
//		}
//	}
}
