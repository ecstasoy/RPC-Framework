package org.example.rpc.registry.registry.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.rpc.common.enums.RegistryCenterType;
import org.example.rpc.registry.zookeeper.ZookeeperHelper;
import org.example.rpc.registry.registry.param.RpcServiceRegistryParam;
import org.example.rpc.registry.registry.param.RpcServiceUnregistryParam;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

/**
 * Zookeeper implementation of service registry.
 */
@Primary
@Component
@Slf4j
public class ZookeeperRpcServiceRegistryImpl extends AbstractRpcServiceRegistry {

  private final ZookeeperHelper zookeeperHelper;

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
  }

  @Override
  void doUnregister(RpcServiceUnregistryParam unregistryParam) {
    // final InetSocketAddress inetSocketAddress = new InetSocketAddress(unRegistryParam.getIp(), unRegistryParam.getPort());
    // zookeeperHelper.removeNode(inetSocketAddress);
  }
}
