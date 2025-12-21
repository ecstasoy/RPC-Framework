package io.ecstasoy.rpc.protocol.serialize;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
  /**
   * -- GETTER --
   *  Get default serializer type.
   *
   * @return default serializer type
   */
  @Getter
  private final SerializerType defaultType;

  /**
   * Constructor.
   *
   * @param properties serializer properties
   */
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

  /**
   * Get serializer by type.
   *
   * @param type type
   * @return serializer
   */
  public Serializer getSerializer(byte type) {
    return serializers.getOrDefault(type,
        serializers.get(defaultType.getType()));
  }
}
