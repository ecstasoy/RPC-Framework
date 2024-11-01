package org.example.rpc.loadbalancer.impl.weight;

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
