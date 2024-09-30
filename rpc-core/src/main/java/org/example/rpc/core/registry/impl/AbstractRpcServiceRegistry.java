package org.example.rpc.core.registry.impl;

import org.example.rpc.core.RpcRequestProcessor;
import org.example.rpc.core.registry.RpcServiceRegistry;
import org.example.rpc.core.registry.param.RpcServiceRegistryParam;
import org.example.rpc.core.registry.param.RpcServiceUnregistryParam;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractRpcServiceRegistry implements RpcServiceRegistry {

  @Override
  public void register(RpcServiceRegistryParam registryParam) {
    doRegister(registryParam);
    RpcRequestProcessor.addRpcBean(registryParam.getServiceName(), registryParam.getRpcBean());
    log.info("[{}] Service: [{}] is registered >> {}:{}", getRegistryCenterType().getName(),
        registryParam.getServiceName(), registryParam.getIp(), registryParam.getPort());
  }

  abstract void doRegister(RpcServiceRegistryParam registryParam);

  @Override
  public void unRegister(RpcServiceUnregistryParam unregistryParam) {
    doUnregister(unregistryParam);
    RpcRequestProcessor.remove(unregistryParam.getServiceName());
    log.info("[{}] Service: [{}] is unregistered >> {}:{}", getRegistryCenterType().getName(),
        unregistryParam.getServiceName(), unregistryParam.getIp(), unregistryParam.getPort());
  }

  abstract void doUnregister(RpcServiceUnregistryParam unregistryParam);
}
