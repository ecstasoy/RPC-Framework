package org.example.rpc.registry;

import org.example.rpc.common.enums.RegistryCenterType;

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
