package org.example.rpc.core.discovery.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.rpc.core.discovery.api.RpcServiceDiscovery;
import org.example.rpc.core.loadbalance.api.LoadBalancer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

/**
 * Abstract class for service discovery.
 */
@Slf4j
public abstract class AbstractRpcServiceDiscovery implements RpcServiceDiscovery {

  @Autowired
  @Qualifier("consistentHashLoadBalancer")
  protected LoadBalancer loadBalancer;

  @Override
  public List<String> getServiceInstaceList(String serviceName) {
    return doGetServiceInstanceList(serviceName);
  }

  abstract List<String> doGetServiceInstanceList(String serviceName);

  @Override
  public String getServiceInstance(String serviceName) {
    final List<String> instances = doGetServiceInstanceList(serviceName);
    if (instances == null || instances.isEmpty()) {
      return null;
    }
    String selected = loadBalancer.select(instances, serviceName);
    log.debug("Load balancer {} selected instance {} for service {}",
        loadBalancer.getStrategy(), selected, serviceName);
    return selected;
  }
}
