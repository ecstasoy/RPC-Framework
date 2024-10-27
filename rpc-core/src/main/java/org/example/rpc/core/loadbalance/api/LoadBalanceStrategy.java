package org.example.rpc.core.loadbalance.api;

public enum LoadBalanceStrategy {
  RANDOM,
  ROUND_ROBIN,
  WEIGHTED, // Weighted Round Robin
  LEAST_ACTIVE,
  CONSISTENT_HASH,
}
