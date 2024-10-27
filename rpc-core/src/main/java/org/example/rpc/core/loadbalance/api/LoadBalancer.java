package org.example.rpc.core.loadbalance.api;

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
  
  // 添加默认方法，这样其他负载均衡器不需要实现这个方法
  default void decrementActive(String instance) {
    // 默认空实现
  }

  // 添加默认方法
  default void adjustWeight(String serviceName, String serviceInstance, int newWeight) {
    // 默认空实现
  }
}
