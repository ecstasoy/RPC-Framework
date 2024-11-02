package org.example.rpc.registry.registry.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.rpc.common.enums.RegistryCenterType;
import org.example.rpc.registry.registry.param.RpcServiceRegistryParam;
import org.example.rpc.registry.registry.param.RpcServiceUnregistryParam;
import org.example.rpc.registry.zookeeper.ZookeeperHelper;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zookeeper implementation of service registry.
 */
@Primary
@Component
@Slf4j
public class ZookeeperRpcServiceRegistryImpl extends AbstractRpcServiceRegistry {

  private final ZookeeperHelper zookeeperHelper;
  private final Map<String, RpcServiceRegistryParam> serviceRegistryParamMap = new ConcurrentHashMap<>();

  /**
   * Constructor.
   *
   * @param zookeeperHelper zookeeper helper
   */
  public ZookeeperRpcServiceRegistryImpl(ZookeeperHelper zookeeperHelper) {
    this.zookeeperHelper = zookeeperHelper;
  }

  @Override
  public RegistryCenterType getRegistryCenterType() {
    return RegistryCenterType.ZOOKEEPER;
  }

  @Override
  void doRegister(RpcServiceRegistryParam registryParam) {
    InetSocketAddress address = new InetSocketAddress(registryParam.getIp(),
        registryParam.getPort());
    zookeeperHelper.createServiceInstanceNode(registryParam.getServiceName(), address);
    serviceRegistryParamMap.put(registryParam.getInstanceId(), registryParam);
  }

  @Scheduled(fixedRate = 5000)
  public void sendHeartbeat() {
    serviceRegistryParamMap.forEach((instanceId, param) -> {
      try {
        zookeeperHelper.updateHealthStatus(param.getServiceName(), instanceId);
        log.debug("Send heartbeat to [{}]", param.getServiceName());
      } catch (Exception e) {
        log.error("Send heartbeat error", e);
      }
    });
  }

  @Override
  void doUnregister(RpcServiceUnregistryParam unregistryParam) {
    // final InetSocketAddress inetSocketAddress = new InetSocketAddress(unRegistryParam.getIp(), unRegistryParam.getPort());
    // zookeeperHelper.removeNode(inetSocketAddress);
  }
}
