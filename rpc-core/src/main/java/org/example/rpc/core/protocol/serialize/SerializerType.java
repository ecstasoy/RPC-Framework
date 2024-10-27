package org.example.rpc.core.protocol.serialize;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Type of serializer.
 */
@AllArgsConstructor
@Getter
public enum SerializerType {

  JSON((byte) 1),
  PROTOBUF((byte) 2),
  HESSIAN((byte) 3),
  KRYO((byte) 4);

  private final byte type;

  public static SerializerType fromType(byte type) {
    for (SerializerType serializerType : values()) {
      if (serializerType.getType() == type) {
        return serializerType;
      }
    }
    throw new IllegalArgumentException("Unknown serializer type: " + type);
  }
}
