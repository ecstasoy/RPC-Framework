package org.example.rpc.core.discovery.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.example.rpc.core.enums.RegistryCenterType;
import org.example.rpc.core.ext.zookeeper.ZookeeperHelper;
import org.example.rpc.core.registry.impl.AbstractRpcServiceRegistry;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Zookeeper implementation of service discovery.
 */
@Service
@Slf4j
@Primary
public class ZookeeperRpcServiceDiscoveryImpl extends AbstractRpcServiceDiscovery {

  private final ZookeeperHelper zookeeperHelper;

  public ZookeeperRpcServiceDiscoveryImpl(ZookeeperHelper zookeeperHelper) {
    this.zookeeperHelper = zookeeperHelper;
  }

  @Override
  List<String> doGetServiceInstanceList(String serviceName) {
    List<String> childrenNodes = zookeeperHelper.getServiceInstanceNode(serviceName);
    if (CollectionUtils.isEmpty(childrenNodes)) {
      log.warn("No available service instances found for " + serviceName);
      return Collections.emptyList();
    }
    return childrenNodes;
  }

  @Override
  public Map<String, List<String>> getAllServiceInstance() {
    return zookeeperHelper.getAllServiceInstanceNode();
  }

  @Override
  public RegistryCenterType getRegistryCenterType() {
    return RegistryCenterType.ZOOKEEPER;
  }
}
