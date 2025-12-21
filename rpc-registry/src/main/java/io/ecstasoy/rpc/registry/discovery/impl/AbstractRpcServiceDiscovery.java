package io.ecstasoy.rpc.registry.discovery.impl;

import lombok.extern.slf4j.Slf4j;
import io.ecstasoy.rpc.loadbalancer.api.LoadBalancer;
import io.ecstasoy.rpc.registry.discovery.api.RpcServiceDiscovery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Abstract class for service discovery.
 *
 * <p>It provides a default implementation for service instance selection.
 *
 * @see ZookeeperRpcServiceDiscoveryImpl
 * @author Kunhua Huang
 */
@Slf4j
public abstract class AbstractRpcServiceDiscovery implements RpcServiceDiscovery {

  @Autowired
  @Qualifier("leastActiveLoadBalancer")
  protected LoadBalancer loadBalancer;

  @Override
  public List<String> getServiceInstaceList(String serviceName) throws ExecutionException {
    return doGetServiceInstanceList(serviceName);
  }

  abstract List<String> doGetServiceInstanceList(String serviceName) throws ExecutionException;

  @Override
  public String getServiceInstance(String serviceName) throws ExecutionException {
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
