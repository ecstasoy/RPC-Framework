package org.example.rpc.core.loadbalance;

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
    int size = serviceInstance.size();
    return serviceInstance.get(Math.abs(position.getAndIncrement() % size));
  }

  @Override
  public LoadBalanceStrategy getStrategy() {
    return LoadBalanceStrategy.ROUND_ROBIN;
  }
}
