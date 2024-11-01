package org.example.rpc.loadbalancer.impl;


import org.example.rpc.loadbalancer.api.LoadBalanceStrategy;
import org.example.rpc.loadbalancer.api.LoadBalancer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class RandomLoadBalancer implements LoadBalancer {

  @Override
  public String select(List<String> serviceInstance, String serviceName) {
    if (serviceInstance == null || serviceInstance.isEmpty()) {
      return null;
    }
    int size = serviceInstance.size();
    return serviceInstance.get(ThreadLocalRandom.current().nextInt(size));
  }

  @Override
  public LoadBalanceStrategy getStrategy() {
    return LoadBalanceStrategy.RANDOM;
  }
}
