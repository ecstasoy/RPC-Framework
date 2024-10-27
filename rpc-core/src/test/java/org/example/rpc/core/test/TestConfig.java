package org.example.rpc.core.test;

import org.example.rpc.core.common.circuit.CircuitBreakerProperties;
import org.example.rpc.core.loadbalance.LoadBalancerFactory;
import org.example.rpc.core.loadbalance.impl.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestConfig {

  @Bean
  @Primary
  public CircuitBreakerProperties circuitBreakerProperties() {
    CircuitBreakerProperties properties = new CircuitBreakerProperties();
    properties.setFailureThreshold(5);
    properties.setResetTimeoutMs(1000);
    properties.setHalfOpenMaxCalls(10);
    return properties;
  }

  @Bean
  public TestService testService(CircuitBreakerProperties properties) {
    return new TestServiceImpl(properties);
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
