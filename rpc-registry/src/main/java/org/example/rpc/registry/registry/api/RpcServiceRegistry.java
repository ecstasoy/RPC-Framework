package org.example.rpc.registry.registry.api;

import org.example.rpc.registry.IRegistryCenter;
import org.example.rpc.registry.registry.param.RpcServiceRegistryParam;
import org.example.rpc.registry.registry.param.RpcServiceUnregistryParam;


/**
 * Interface for registry center.
 */
public interface RpcServiceRegistry extends IRegistryCenter {

  /**
   * Register service.
   *
   * @param registryParam parameters for registration
   */
  void register(RpcServiceRegistryParam registryParam);

  /**
   * Unregister service.
   *
   * @param unregistryParam parameters for unregistration
   */
  void unRegister(RpcServiceUnregistryParam unregistryParam);

}
