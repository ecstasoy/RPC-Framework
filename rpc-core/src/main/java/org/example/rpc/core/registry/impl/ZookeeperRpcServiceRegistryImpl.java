package org.example.rpc.core.registry.impl;

import org.example.rpc.core.enums.RegistryCenterType;
import org.example.rpc.core.ext.zookeeper.ZookeeperHelper;
import org.example.rpc.core.registry.param.RpcServiceRegistryParam;
import org.example.rpc.core.registry.param.RpcServiceUnregistryParam;
import lombok.extern.slf4j.Slf4j;
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
