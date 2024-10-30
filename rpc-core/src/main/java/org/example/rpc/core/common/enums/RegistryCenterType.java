package org.example.rpc.core.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Type of registry center
 *
 * @author Kunhua Huang
 */
@AllArgsConstructor
@Getter
public enum RegistryCenterType {
  ZOOKEEPER("Zookeeper");
  private final String name;
}