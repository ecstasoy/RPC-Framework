package org.example.rpc.core.loadbalance.impl;

import org.example.rpc.core.loadbalance.api.LoadBalanceStrategy;
import org.example.rpc.core.loadbalance.api.LoadBalancer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RoundRobinLoadBalancer implements LoadBalancer {

  private final AtomicInteger position = new AtomicInteger(0);

  @Override
  public String select(List<String> serviceInstance, String serviceName) {
    if (serviceInstance == null || serviceInstance.isEmpty()) {
      return null;
    }
    
    // Use compareAndSet to ensure thread safety
    for (;;) {
      int current = position.get();
      int next = (current + 1) % serviceInstance.size();
      if (position.compareAndSet(current, next)) {
        return serviceInstance.get(current);
      }
    }
  }

  @Override
  public LoadBalanceStrategy getStrategy() {
    return LoadBalanceStrategy.ROUND_ROBIN;
  }
}
