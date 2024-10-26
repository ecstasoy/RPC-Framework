package org.example.rpc.core.loadbalance;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Least active load balancer
 */
@Component
public class LeastActiveLoadBalancer implements LoadBalancer {
  private final Map<String, AtomicInteger> activeCountMap = new ConcurrentHashMap<>();

  @Override
  public String select(List<String> serviceInstance, String serviceName) {
    if (serviceInstance == null || serviceInstance.isEmpty()) {
      return null;
    }
    String selectedInstance = null;
    int leastActive = Integer.MAX_VALUE;

    for (String instance : serviceInstance) {
      AtomicInteger activeCount = activeCountMap.computeIfAbsent(instance, k -> new AtomicInteger(0));
      int currentActive = activeCount.get();
      if (currentActive < leastActive) {
        leastActive = currentActive;
        selectedInstance = instance;
      }
    }

    if (selectedInstance == null) {
      activeCountMap.get(selectedInstance).incrementAndGet();
    }

    return selectedInstance;
  }

  /**
   * Decrement active count
   * @param instance instance
   */
  public void decrementActive(String instance) {
    AtomicInteger activeCount = activeCountMap.get(instance);
    if (activeCount != null) {
      activeCount.decrementAndGet();
    }
  }

  @Override
  public LoadBalanceStrategy getStrategy() {
    return LoadBalanceStrategy.LEAST_ACTIVE;
  }
}
