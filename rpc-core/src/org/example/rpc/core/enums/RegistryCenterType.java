package org.example.rpc.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Type of registry center.
 */
@AllArgsConstructor
@Getter
public enum RegistryCenterType {

  /**
   * Zookeeper
   */
  ZOOKEEPER("Zookeeper");

  private final String name;
}