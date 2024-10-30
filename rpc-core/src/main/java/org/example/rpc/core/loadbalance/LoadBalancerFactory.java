package org.example.rpc.core.loadbalance;

import org.example.rpc.core.loadbalance.api.LoadBalanceStrategy;
import org.example.rpc.core.loadbalance.api.LoadBalancer;
import org.example.rpc.core.loadbalance.impl.*;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Load balancer factory.
 *
 * <p>Factory class to get load balancer by strategy.
 *
 * @author Kunhua Huang
 */
@Component
public class LoadBalancerFactory {
  private final Map<LoadBalanceStrategy, LoadBalancer> loadBalancers;

  /**
   * Constructor.
   *
   * @param randomLoadBalancer      random load balancer
   * @param roundRobinLoadBalancer  round robin load balancer
   * @param weightedLoadBalancer    weighted load balancer
   * @param leastActiveLoadBalancer least active load balancer
   * @param consistentHashLoadBalancer consistent hash load balancer
   */
  public LoadBalancerFactory(
      RandomLoadBalancer randomLoadBalancer,
      RoundRobinLoadBalancer roundRobinLoadBalancer,
      WeightedLoadBalancer weightedLoadBalancer,
      LeastActiveLoadBalancer leastActiveLoadBalancer,
      ConsistentHashLoadBalancer consistentHashLoadBalancer) {

    loadBalancers = new ConcurrentHashMap<>();
    loadBalancers.put(LoadBalanceStrategy.RANDOM, randomLoadBalancer);
    loadBalancers.put(LoadBalanceStrategy.ROUND_ROBIN, roundRobinLoadBalancer);
    loadBalancers.put(LoadBalanceStrategy.WEIGHTED, weightedLoadBalancer);
    loadBalancers.put(LoadBalanceStrategy.LEAST_ACTIVE, leastActiveLoadBalancer);
    loadBalancers.put(LoadBalanceStrategy.CONSISTENT_HASH, consistentHashLoadBalancer);
  }

  /**
   * Get load balancer by strategy.
   *
   * @param strategy load balance strategy
   * @return load balancer
   */
  public LoadBalancer getLoadBalancer(LoadBalanceStrategy strategy) {
    LoadBalancer loadBalancer = loadBalancers.get(strategy);
    if (loadBalancer == null) {
      throw new IllegalArgumentException("Unsupported load balance strategy: " + strategy);
    }
    return loadBalancer;
  }

  /**
   * Decrement active count of service instance.
   *
   * @param strategy load balance strategy
   * @param instance service instance
   */
  public void decrementActive(LoadBalanceStrategy strategy, String instance) {
    LoadBalancer loadBalancer = getLoadBalancer(strategy);
    loadBalancer.decrementActive(instance);
  }

  /**
   * Adjust weight of service instance.
   *
   * @param strategy       load balance strategy
   * @param serviceName     service name
   * @param serviceInstance service instance
   * @param newWeight       new weight
   */
  public void adjustWeight(LoadBalanceStrategy strategy, String serviceName, String serviceInstance, int newWeight) {
    LoadBalancer loadBalancer = getLoadBalancer(strategy);
    loadBalancer.adjustWeight(serviceName, serviceInstance, newWeight);
  }
}
