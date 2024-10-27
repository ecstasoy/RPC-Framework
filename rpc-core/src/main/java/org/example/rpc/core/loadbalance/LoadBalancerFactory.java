package org.example.rpc.core.loadbalance;

import org.example.rpc.core.loadbalance.api.LoadBalanceStrategy;
import org.example.rpc.core.loadbalance.api.LoadBalancer;
import org.example.rpc.core.loadbalance.impl.*;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Load balancer factory.
 */
@Component
public class LoadBalancerFactory {
  private final Map<LoadBalanceStrategy, LoadBalancer> loadBalancers;

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

  public LoadBalancer getLoadBalancer(LoadBalanceStrategy strategy) {
    LoadBalancer loadBalancer = loadBalancers.get(strategy);
    if (loadBalancer == null) {
      throw new IllegalArgumentException("Unsupported load balance strategy: " + strategy);
    }
    return loadBalancer;
  }

  public void decrementActive(LoadBalanceStrategy strategy, String instance) {
    LoadBalancer loadBalancer = getLoadBalancer(strategy);
    loadBalancer.decrementActive(instance);
  }

  public void adjustWeight(LoadBalanceStrategy strategy, String serviceName, String serviceInstance, int newWeight) {
    LoadBalancer loadBalancer = getLoadBalancer(strategy);
    loadBalancer.adjustWeight(serviceName, serviceInstance, newWeight);
  }
}
