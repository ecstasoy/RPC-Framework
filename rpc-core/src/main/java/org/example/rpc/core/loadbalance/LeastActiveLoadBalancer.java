package org.example.rpc.core.loadbalance;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

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

    // 1. 找出当前最小活跃数
    int leastActive = Integer.MAX_VALUE;
    List<String> leastActiveInstances = new ArrayList<>();
    
    for (String instance : serviceInstance) {
      AtomicInteger count = activeCountMap.computeIfAbsent(instance, k -> new AtomicInteger(0));
      int active = count.get();
      
      if (active < leastActive) {
        leastActive = active;
        leastActiveInstances.clear();
        leastActiveInstances.add(instance);
      } else if (active == leastActive) {
        leastActiveInstances.add(instance);
      }
    }

    // 2. 如果有多个最小活跃数的实例，随机选择一个
    String selectedInstance;
    if (leastActiveInstances.size() == 1) {
      selectedInstance = leastActiveInstances.get(0);
    } else {
      selectedInstance = leastActiveInstances.get(ThreadLocalRandom.current().nextInt(leastActiveInstances.size()));
    }

    // 3. 增加选中实例的活跃数
    activeCountMap.get(selectedInstance).incrementAndGet();
    return selectedInstance;
  }

  /**
   * 服务调用完成后减少活跃数
   */
  public void decrementActive(String instance) {
    AtomicInteger count = activeCountMap.get(instance);
    if (count != null && count.get() > 0) {
      count.decrementAndGet();
    }
  }

  @Override
  public LoadBalanceStrategy getStrategy() {
    return LoadBalanceStrategy.LEAST_ACTIVE;
  }
}
