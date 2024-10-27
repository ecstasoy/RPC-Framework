package org.example.rpc.core.loadbalance.impl.weight;

import lombok.Data;

/**
 * Service weight
 */
@Data
public class ServiceWeight {
  private String serviceInstance;
  private int weight;
  private int currentWeight;
}
