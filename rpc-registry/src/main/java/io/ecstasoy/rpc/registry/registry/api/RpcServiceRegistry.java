package io.ecstasoy.rpc.registry.registry.api;

import io.ecstasoy.rpc.registry.IRegistryCenter;
import io.ecstasoy.rpc.registry.registry.param.RpcServiceRegistryParam;
import io.ecstasoy.rpc.registry.registry.param.RpcServiceUnregistryParam;


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
