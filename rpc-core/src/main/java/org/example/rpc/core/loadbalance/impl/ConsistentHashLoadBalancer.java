package org.example.rpc.core.loadbalance.impl;

import org.example.rpc.core.loadbalance.api.LoadBalanceStrategy;
import org.example.rpc.core.loadbalance.api.LoadBalancer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

@Component
public class ConsistentHashLoadBalancer implements LoadBalancer {

  private final int virtualNodes;
  private final SortedMap<Integer, String> circle = new TreeMap<>();

  public ConsistentHashLoadBalancer() {
    this.virtualNodes = 150; // default value
  }

  @Override
  public String select(List<String> serviceInstance, String serviceName) {
    if (serviceInstance == null || serviceInstance.isEmpty()) {
      return null;
    }

    buildHashCircle(serviceInstance);
    int hash = getHash(serviceName);

    SortedMap<Integer, String> tailMap = circle.tailMap(hash);
    int nodeHash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();

    return circle.get(nodeHash);
  }

  private void buildHashCircle(List<String> serviceInstance) {
    circle.clear();
    for (String instance : serviceInstance) {
      for (int i = 0; i < virtualNodes; i++) {
        String virtualNodeName = instance + "#" + i;
        circle.put(getHash(virtualNodeName), instance);
      }
    }
  }

  private int getHash(String key) {
    final int p = 16777619;
    int hash = (int) 2166136261L;
    for (int i = 0; i < key.length(); i++) {
      hash = (hash ^ key.charAt(i)) * p;
    }
    hash += hash << 13;
    hash ^= hash >> 7;
    hash += hash << 3;
    hash ^= hash >> 17;
    hash += hash << 5;
    return hash < 0 ? Math.abs(hash) : hash;
  }

  @Override
  public LoadBalanceStrategy getStrategy() {
    return LoadBalanceStrategy.CONSISTENT_HASH;
  }
}
