package org.example.rpc.core.loadbalance.api;

/**
 * Load balance strategy.
 *
 * @author Kunhua Huang
 */
public enum LoadBalanceStrategy {
  RANDOM,
  ROUND_ROBIN,
  WEIGHTED, // Weighted Round Robin
  LEAST_ACTIVE,
  CONSISTENT_HASH,
}
