package org.example.rpc.loadbalancer.impl;

import org.example.rpc.loadbalancer.impl.weight.ServiceWeight;
import org.example.rpc.loadbalancer.api.LoadBalanceStrategy;
import org.example.rpc.loadbalancer.api.LoadBalancer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WeightedLoadBalancer implements LoadBalancer {
  private final Map<String, List<ServiceWeight>> serviceWeightMap = new ConcurrentHashMap<>();

  @Override
  public String select(List<String> serviceInstance, String serviceName) {
    if (serviceInstance == null || serviceInstance.isEmpty()) {
      return null;
    }

    List<ServiceWeight> weights = serviceWeightMap.computeIfAbsent(serviceName, k -> {
      List<ServiceWeight> list = new ArrayList<>();
      for (String instance : serviceInstance) {
        ServiceWeight serviceWeight = new ServiceWeight();
        serviceWeight.setServiceInstance(instance);
        serviceWeight.setWeight(10); // default weight is 10
        serviceWeight.setCurrentWeight(0);
        list.add(serviceWeight);
      }
      return list;
    });

    return smoothWeightedRoundRobin(weights);
  }

  /**
   * Smooth weighted round-robin algorithm
   * @param weights service weight list
   * @return selected instance
   */
  private String smoothWeightedRoundRobin(List<ServiceWeight> weights) {
    int totalWeight = 0;
    ServiceWeight maxCurrentWeight = null;

    for (ServiceWeight weight : weights) {
      totalWeight += weight.getWeight();
      weight.setCurrentWeight(weight.getCurrentWeight() + weight.getWeight());

      if (maxCurrentWeight == null || weight.getCurrentWeight() > maxCurrentWeight.getCurrentWeight()) {
        maxCurrentWeight = weight;
      }
    }

    if (maxCurrentWeight != null) {
      maxCurrentWeight.setCurrentWeight(maxCurrentWeight.getCurrentWeight() - totalWeight);
      return maxCurrentWeight.getServiceInstance();
    }

    return null;
  }

  /**
   * Adjust weight of service instance
   * @param serviceName service name
   * @param serviceInstance service instance
   * @param newWeight new weight
   */
  public void adjustWeight(String serviceName, String serviceInstance, int newWeight) {
    List<ServiceWeight> weights = serviceWeightMap.get(serviceName);
    if (weights != null) {
      for (ServiceWeight weight : weights) {
        if (weight.getServiceInstance().equals(serviceInstance)) {
          weight.setWeight(newWeight);
          break;
        }
      }
    }
  }

  @Override
  public LoadBalanceStrategy getStrategy() {
    return LoadBalanceStrategy.WEIGHTED;
  }
}
