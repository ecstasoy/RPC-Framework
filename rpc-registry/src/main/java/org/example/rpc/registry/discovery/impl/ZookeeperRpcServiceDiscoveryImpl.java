package org.example.rpc.registry.discovery.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.example.rpc.common.enums.RegistryCenterType;
import org.example.rpc.registry.zookeeper.ZookeeperHelper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Zookeeper implementation of service discovery.
 */
@Service
@Slf4j
@Primary
public class ZookeeperRpcServiceDiscoveryImpl extends AbstractRpcServiceDiscovery {

  private final ZookeeperHelper zookeeperHelper;

  /**
   * Constructor.
   *
   * @param zookeeperHelper zookeeper helper
   */
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
    return childrenNodes.stream()
        .filter(instanceId -> zookeeperHelper.checkHealthNode(serviceName, instanceId))
        .collect(Collectors.toList());
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
