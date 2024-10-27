package org.example.rpc.core.protocol.serialize;

/**
 * Serializer interface.
 */
public interface Serializer {

  /**
   * Serialize object.
   *
   * @param obj object
   * @return byte array
   */
  byte[] serialize(Object obj);

  /**
   * Deserialize.
   *
   * @param bytes byte array
   * @param classType class type
   * @param <T> class type
   * @return object
   */
  <T> T deSerialize(byte[] bytes, Class<T> classType);

  /**
   * Get serializer type.
   *
   * @return serializer type
   */
  SerializerType getSerializerType();
}
