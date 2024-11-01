package org.example.rpc.protocol.serialize;

/**
 * Serializer interface.
 */
public interface Serializer {

  /**
   * Serialize object.
   *
   * @param obj object
   * @return <T> byte array
   */
  <T> byte[] serialize(T obj) throws Exception;

  /**
   * Deserialize.
   *
   * @param bytes byte array
   * @param classType class type
   * @param <T> class type
   * @return object
   */
  <T> T deSerialize(byte[] bytes, Class<T> classType) throws Exception;

  /**
   * Get serializer type.
   *
   * @return serializer type
   */
  SerializerType getSerializerType();
}
