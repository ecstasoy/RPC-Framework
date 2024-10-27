package org.example.rpc.core.protocol.serialize;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serializer factory.
 */
@Slf4j
@Component
public class SerializerFactory {

  private static final Map<Byte, Serializer> SERIALIZERS = new ConcurrentHashMap<>();

  static {
    // Use SPI to load all implementations of Serializer interface
    ServiceLoader<Serializer> serviceLoader = ServiceLoader.load(Serializer.class);
    for (Serializer serializer : serviceLoader) {
      SERIALIZERS.put(serializer.getSerializerType().getType(), serializer);
      log.info("Found serializer: [{}]", serializer.getClass().getCanonicalName());
    }
  }

  public static Serializer getSerializer(byte type) {
    Serializer serializer = SERIALIZERS.get(type);
    if (serializer == null) {
      throw new RuntimeException("Unknown serializer type: " + type);
    }
    return serializer;
  }

}
