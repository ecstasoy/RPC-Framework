package org.example.rpc.core.loadbalance;

import java.util.List;

/**
 * Load balancer interface.
 */
public interface LoadBalancer {

  /**
   * Select an instance from serviceInstance list
   * @param serviceInstance service instance list
   * @param serviceName service name
   * @return selected instance
   */
  String select(List<String> serviceInstance, String serviceName);

  /**
   * Get current strategy.
   *
   * @return LoadBalanceStrategy
   */
  LoadBalanceStrategy getStrategy();
}
