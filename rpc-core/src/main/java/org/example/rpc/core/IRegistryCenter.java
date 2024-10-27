package org.example.rpc.core;

import org.example.rpc.core.common.enums.RegistryCenterType;

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
