package com.r.data.cache.manager;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import com.r.data.cache.constants.RedisConstants;

/**
 * Redis cluster manager
 *
 * @author Ranjith
 * @since 1.0
 */
public class RedisClusterManager
{
	private Properties properties;

	private static JedisCluster jedisCluster;

	private static RedisClusterManager instance;

	private RedisClusterManager(Properties properties) {
		this.properties = properties;
	}

	public static RedisClusterManager createInstance(Properties properties) {
		instance = new RedisClusterManager(properties);
		instance.connect();
		return instance;
	}

	public final static RedisClusterManager getInstance() throws Exception {
		return instance;
	}

	public void connect() {
		// Create and set a JedisPoolConfig
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		// Maximum active connections to Redis instance
		int maxActive = Integer.parseInt(properties.getProperty(RedisConstants.MAX_ACTIVE, RedisConstants.DEFAULT_MAX_ACTIVE_VALUE));
		poolConfig.setMaxTotal(maxActive);
		// Tests whether connection is dead when connection
		// retrieval method is called
		boolean testOnBorrow = Boolean.parseBoolean(properties.getProperty(RedisConstants.TEST_ONBORROW, RedisConstants.DEFAULT_TEST_ONBORROW_VALUE));
		poolConfig.setTestOnBorrow(testOnBorrow);
		/* Some extra configuration */
		// Tests whether connection is dead when returning a
		// connection to the pool
		boolean testOnReturn = Boolean.parseBoolean(properties.getProperty(RedisConstants.TEST_ONRETURN, RedisConstants.DEFAULT_TEST_ONRETURN_VALUE));
		poolConfig.setTestOnReturn(testOnReturn);
		// Number of connections to Redis that just sit there
		// and do nothing
		int maxIdle = Integer.parseInt(properties.getProperty(RedisConstants.MAX_IDLE, RedisConstants.DEFAULT_MAX_ACTIVE_VALUE));
		poolConfig.setMaxIdle(maxIdle);
		// Minimum number of idle connections to Redis
		// These can be seen as always open and ready to serve
		int minIdle = Integer.parseInt(properties.getProperty(RedisConstants.MIN_IDLE, RedisConstants.DEFAULT_MIN_IDLE_VALUE));
		poolConfig.setMinIdle(minIdle);
		// Tests whether connections are dead during idle periods
		boolean testWhileIdle = Boolean.parseBoolean(properties.getProperty(RedisConstants.TEST_WHILEIDLE, RedisConstants.DEFAULT_TEST_WHILEIDLE_VALUE));
		poolConfig.setTestWhileIdle(testWhileIdle);
		// Maximum number of connections to test in each idle check
		int testNumPerEviction = Integer.parseInt(properties.getProperty(RedisConstants.TEST_NUMPEREVICTION, RedisConstants.DEFAULT_TEST_NUMPEREVICTION_VALUE));
		poolConfig.setNumTestsPerEvictionRun(testNumPerEviction);
		// Idle connection checking period
		long timeBetweenEviction = Long.parseLong(properties.getProperty(RedisConstants.TIME_BETWEENEVICTION, RedisConstants.DEFAULT_TIME_BETWEENEVICTION_VALUE));
		poolConfig.setTimeBetweenEvictionRunsMillis(timeBetweenEviction);
		// Create the jedis cluster
		jedisCluster = new JedisCluster(getJedisClusterNodesSet(properties.getProperty(RedisConstants.HOSTS, Protocol.DEFAULT_HOST.concat(":").concat(String.valueOf(Protocol.DEFAULT_PORT)))), poolConfig);
	}

	/*
	 * method to get the cluster nodes
	 */
	private Set<HostAndPort> getJedisClusterNodesSet(String hosts) {
		Set<HostAndPort> nodes = new HashSet<HostAndPort>();
		hosts = hosts.replaceAll("\\s", "");
		String[] hostPorts = hosts.split(",");
		for (String hostPort : hostPorts) {
			String[] hostPortArr = hostPort.split(":");
			nodes.add(new HostAndPort(hostPortArr[0], Integer.valueOf(hostPortArr[1])));
		}
		return nodes;
	}

	public JedisCluster getJedis() {
		return jedisCluster;
	}
}