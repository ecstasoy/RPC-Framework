package org.example.rpc.core.loadbalance.api;

import java.util.List;

/**
 * Load balancer interface.
 *
 * <p>Load balancer is used to select an instance from serviceInstance list.
 * <p>Load balancer should be stateless, so it can be shared by multiple threads.
 * <p>Load balancer should be thread-safe.
 *
 * @author Kunhua Huang
 */
public interface LoadBalancer {

  /**
   * Select an instance from serviceInstance list
   *
   * @param serviceInstance service instance list
   * @param serviceName     service name
   * @return selected instance
   */
  String select(List<String> serviceInstance, String serviceName);

  /**
   * Get current strategy.
   *
   * @return LoadBalanceStrategy
   */
  LoadBalanceStrategy getStrategy();

  /**
   * Default method to increment active count.
   *
   * @param instance service instance
   */
  default void decrementActive(String instance) {
  }

  /**
   * Default method to decrement active count.
   *
   * @param serviceName     service name
   * @param serviceInstance service instance
   * @param newWeight       new weight
   */
  default void adjustWeight(String serviceName, String serviceInstance, int newWeight) {
  }
}
