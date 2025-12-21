package io.ecstasoy.rpc.core.test;

import io.ecstasoy.rpc.common.circuit.CircuitBreaker;
import io.ecstasoy.rpc.common.circuit.CircuitBreakerProperties;
import io.ecstasoy.rpc.loadbalancer.LoadBalancerFactory;
import io.ecstasoy.rpc.loadbalancer.impl.*;
import io.ecstasoy.rpc.registry.discovery.impl.ZookeeperRpcServiceDiscoveryImpl;
import io.ecstasoy.rpc.registry.health.ServiceHealthManager;
import io.ecstasoy.rpc.registry.registry.HeartbeatManager;
import io.ecstasoy.rpc.registry.registry.api.RpcServiceRegistry;
import io.ecstasoy.rpc.registry.registry.impl.ZookeeperRpcServiceRegistryImpl;
import io.ecstasoy.rpc.registry.zookeeper.ZookeeperHelper;
import io.ecstasoy.rpc.registry.zookeeper.ZookeeperProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Test configuration.
 *
 * <p>It provides beans for testing.
 *
 * @author Kunhua Huang
 */
@Configuration
@EnableConfigurationProperties({ZookeeperProperties.class, CircuitBreakerProperties.class})
public class TestConfig {

  @Bean
  @Primary
  public TestService testService() {
    return new TestServiceImpl();
  }

  @Bean
  public HeartbeatManager heartbeatManager(ServiceHealthManager serviceHealthManager, ZookeeperHelper zookeeperHelper) {
    return new HeartbeatManager(serviceHealthManager, zookeeperHelper);
  }

  @Bean
  public CircuitBreaker circuitBreaker() {
    return new CircuitBreaker(new CircuitBreakerProperties());
  }

  @Bean
  public ZookeeperRpcServiceDiscoveryImpl rpcServiceDiscovery(
      ZookeeperHelper zookeeperHelper,
      ServiceHealthManager serviceHealthManager) {
    return new ZookeeperRpcServiceDiscoveryImpl(zookeeperHelper);
  }

  @Bean
  public ZookeeperHelper zookeeperHelper(ZookeeperProperties zookeeperProperties, ServiceHealthManager serviceHealthManager) {
    return new ZookeeperHelper(zookeeperProperties, serviceHealthManager);
  }
  @Bean
  public RpcServiceRegistry rpcServiceRegistry(ZookeeperHelper zookeeperHelper) {
    return new ZookeeperRpcServiceRegistryImpl(heartbeatManager(serviceHealthManager(), zookeeperHelper), zookeeperHelper, serviceHealthManager());
  }

  @Bean
  public ServiceHealthManager serviceHealthManager() {
    return new ServiceHealthManager();
  }

  @Bean
  public RandomLoadBalancer randomLoadBalancer() {
    return new RandomLoadBalancer();
  }

  @Bean
  public RoundRobinLoadBalancer roundRobinLoadBalancer() {
    return new RoundRobinLoadBalancer();
  }

  @Bean
  public WeightedLoadBalancer weightedLoadBalancer() {
    return new WeightedLoadBalancer();
  }

  @Bean
  public LeastActiveLoadBalancer leastActiveLoadBalancer() {
    return new LeastActiveLoadBalancer();
  }

  @Bean
  public ConsistentHashLoadBalancer consistentHashLoadBalancer() {
    return new ConsistentHashLoadBalancer();
  }

  @Bean
  public LoadBalancerFactory loadBalancerFactory(
      RandomLoadBalancer randomLoadBalancer,
      RoundRobinLoadBalancer roundRobinLoadBalancer,
      WeightedLoadBalancer weightedLoadBalancer,
      LeastActiveLoadBalancer leastActiveLoadBalancer,
      ConsistentHashLoadBalancer consistentHashLoadBalancer) {
    return new LoadBalancerFactory(
        randomLoadBalancer,
        roundRobinLoadBalancer,
        weightedLoadBalancer,
        leastActiveLoadBalancer,
        consistentHashLoadBalancer
    );
  }
}
