package org.example.rpc.core.serialize;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Serializer factory.
 */
@Slf4j
@Component
public class SerializerFactory {


  private final List<Serializer> serializerList;

  /**
   * Constructor.
   *
   * @param serializerList serializer list
   */
  public SerializerFactory(List<Serializer> serializerList) {
    this.serializerList = serializerList;
  }

  /**
   * Get serializer by type.
   *
   * @param type type
   * @return serializer
   */
  public Serializer getSerializer(byte type) {
    for (Serializer serializer : serializerList) {
      if (serializer.getSerializerType().getType() == type) {
        log.debug("Serializer found for type: {}", type);
        return serializer;
      }
    }

    // Log and throw an exception when serializer is not found
    log.error("No serializer found for type: {}", type);
    throw new IllegalArgumentException("No serializer found for type: " + type);
  }

}
