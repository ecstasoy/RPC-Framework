package org.example.rpc.core.protocol.serialize;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Type of serializer.
 */
@AllArgsConstructor
@Getter
public enum SerializerType {

  JSON((byte) 1);

  private final byte type;
}
