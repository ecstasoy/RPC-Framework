package org.example.rpc.core.serialize;

/**
 * Serializer interface.
 */
public interface Serializer {

  byte[] serialize(Object obj);

  <T> T deSerialize(byte[] bytes, Class<T> classType);

  SerializerType getSerializerType();
}
