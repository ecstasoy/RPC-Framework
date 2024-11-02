package org.example.rpc.registry.registry.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.rpc.common.enums.RegistryCenterType;
import org.example.rpc.registry.registry.param.RpcServiceRegistryParam;
import org.example.rpc.registry.registry.param.RpcServiceUnregistryParam;
import org.example.rpc.registry.zookeeper.ZookeeperHelper;
import org.example.rpc.registry.health.ServiceHealthManager;
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
  private final ServiceHealthManager serviceHealthManager;

  /**
   * Constructor.
   *
   * @param zookeeperHelper zookeeper helper
   */
  public ZookeeperRpcServiceRegistryImpl(ZookeeperHelper zookeeperHelper, ServiceHealthManager serviceHealthManager) {
    this.zookeeperHelper = zookeeperHelper;
    this.serviceHealthManager = serviceHealthManager;
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
        serviceHealthManager.recordHeartbeatSuccess(param.getServiceName(), instanceId, zookeeperHelper);
        log.debug("Send heartbeat to [{}]", param.getServiceName());
      } catch (Exception e) {
        log.error("Send heartbeat error", e);
        serviceHealthManager.recordHeartbeatFailure(param.getServiceName(), instanceId, zookeeperHelper);
      }
    });
  }

  @Override
  void doUnregister(RpcServiceUnregistryParam unregistryParam) {
    // final InetSocketAddress inetSocketAddress = new InetSocketAddress(unRegistryParam.getIp(), unRegistryParam.getPort());
    // zookeeperHelper.removeNode(inetSocketAddress);
  }
}
