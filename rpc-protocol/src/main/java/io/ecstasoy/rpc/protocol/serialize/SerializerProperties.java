package io.ecstasoy.rpc.protocol.serialize;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Serializer properties.
 */
@Data
@Component
@ConfigurationProperties(prefix = "rpc.serializer")
public class SerializerProperties {

  private SerializerType type = SerializerType.PROTOBUF;
  private boolean enableAutoType = true;
  private int maxObjectSize = 10485760;
}
