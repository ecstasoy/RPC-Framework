package org.example.rpc.loadbalancer.impl;

import org.example.rpc.loadbalancer.api.LoadBalanceStrategy;
import org.example.rpc.loadbalancer.api.LoadBalancer;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

@Component
public class ConsistentHashLoadBalancer implements LoadBalancer {

  private final int virtualNodes;
  private final SortedMap<Integer, String> circle = new ConcurrentSkipListMap<>();
  private Set<String> currentInstances;

  public ConsistentHashLoadBalancer() {
    this.virtualNodes = 150;
  }

  @Override
  public String select(List<String> serviceInstance, String serviceName) {
    if (serviceInstance == null || serviceInstance.isEmpty()) {
      return null;
    }

    String param = extractParam(serviceName);
    String actualServiceName = param != null ? serviceName : serviceName + "#" + System.nanoTime();

    Set<String> newInstances = new HashSet<>(serviceInstance);
    if (!newInstances.equals(currentInstances)) {
      buildHashCircle(serviceInstance);
      currentInstances = newInstances;
    }

    int hash = getHash(actualServiceName);
    SortedMap<Integer, String> tailMap = circle.tailMap(hash);
    int nodeHash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();

    return circle.get(nodeHash);
  }

  private String extractParam(String serviceName) {
    int index = serviceName.indexOf('#');
    return index > 0 ? serviceName.substring(index + 1) : null;
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
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(key.getBytes());
      byte[] digest = md.digest();
      int hash = ((digest[0] & 0xFF) << 24) | ((digest[1] & 0xFF) << 16)
          | ((digest[2] & 0xFF) << 8) | (digest[3] & 0xFF);
      return hash < 0 ? Math.abs(hash) : hash;
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("MD5 algorithm not found", e);
    }
  }

  @Override
  public LoadBalanceStrategy getStrategy() {
    return LoadBalanceStrategy.CONSISTENT_HASH;
  }
}
