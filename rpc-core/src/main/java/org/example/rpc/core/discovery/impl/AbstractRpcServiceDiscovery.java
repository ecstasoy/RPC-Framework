package org.example.rpc.core.discovery.impl;

import org.example.rpc.core.discovery.RpcServiceDiscovery;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Abstract class for service discovery.
 */
@Slf4j
public abstract class AbstractRpcServiceDiscovery implements RpcServiceDiscovery {

  @Override
  public List<String> getServiceInstaceList(String serviceName) {
    return doGetServiceInstanceList(serviceName);
  }

  abstract List<String> doGetServiceInstanceList(String serviceName);

  @Override
  public String getServiceInstance(String serviceName) {
    final List<String> list = doGetServiceInstanceList(serviceName);
    if (list == null) {
      return null;
    }
    return list.stream().findFirst().orElse(null);
  }
}
