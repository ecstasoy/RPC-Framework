package org.example.rpc.registry.registry.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.rpc.processor.RpcRequestProcessor;
import org.example.rpc.registry.registry.api.RpcServiceRegistry;
import org.example.rpc.registry.registry.param.RpcServiceRegistryParam;
import org.example.rpc.registry.registry.param.RpcServiceUnregistryParam;

/**
 * Abstract class for service registry.
 */
@Slf4j
public abstract class AbstractRpcServiceRegistry implements RpcServiceRegistry {

  @Override
  public void register(RpcServiceRegistryParam registryParam) {
    doRegister(registryParam);
    RpcRequestProcessor.addService(registryParam.getServiceName(), registryParam.getRpcBean());
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
