package io.ecstasoy.rpc.registry;

import io.ecstasoy.rpc.common.enums.RegistryCenterType;

/**
 * Interface for registry center.
 */
public interface IRegistryCenter {

  /**
   * Gets the type of registry center.
   *
   * @return type of registry center
   */
  RegistryCenterType getRegistryCenterType();
}
