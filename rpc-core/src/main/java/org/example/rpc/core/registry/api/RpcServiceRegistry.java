package org.example.rpc.core.registry.api;

import org.example.rpc.core.IRegistryCenter;
import org.example.rpc.core.registry.param.RpcServiceRegistryParam;
import org.example.rpc.core.registry.param.RpcServiceUnregistryParam;


/**
 * Interface for registry center.
 */
public interface RpcServiceRegistry extends IRegistryCenter {

  /**
   * Gets the type of registry center.
   *
   * @return type of registry center
   */
  void register(RpcServiceRegistryParam registryParam);

  /**
   * Unregister service.
   *
   * @param unregistryParam parameters for unregistration
   */
  void unRegister(RpcServiceUnregistryParam unregistryParam);

}
