package org.example.rpc.protocol.serialize;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serializer factory.
 */
@Slf4j
@Component
public class SerializerFactory {

  private final Map<Byte, Serializer> serializers = new ConcurrentHashMap<>();
  private final SerializerType defaultType;

  @Autowired
  public SerializerFactory(SerializerProperties properties) {
    this.defaultType = properties.getType();
    loadSerializers();
  }

  private void loadSerializers() {
    ServiceLoader<Serializer> serviceLoader = ServiceLoader.load(Serializer.class);
    for (Serializer serializer : serviceLoader) {
      serializers.put(serializer.getSerializerType().getType(), serializer);
      log.info("Loaded serializer: [{}]", serializer.getClass().getCanonicalName());
    }
  }

  public Serializer getSerializer(byte type) {
    return serializers.getOrDefault(type,
        serializers.get(defaultType.getType()));
  }

  public SerializerType getDefaultType() {
    return defaultType;
  }

}
