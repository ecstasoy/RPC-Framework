package org.example.rpc.core.loadbalance;

public enum LoadBalanceStrategy {
  RANDOM,
  ROUND_ROBIN,
  WEIGHTED, // Weighted Round Robin
  LEAST_ACTIVE,
}
