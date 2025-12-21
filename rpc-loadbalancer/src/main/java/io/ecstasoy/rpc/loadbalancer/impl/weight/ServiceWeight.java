package io.ecstasoy.rpc.loadbalancer.impl.weight;

import lombok.Data;

/**
 * The weight of the service instance.
 */
@Data
public class ServiceWeight {
  private String serviceInstance;
  private int weight;
  private int currentWeight;
}
