package org.example.rpc.transport.server;

import lombok.Data;
import org.example.rpc.protocol.serialize.SerializerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Properties of Netty server.
 */
@Data
@Component
public class NettyServerProperties {

  @Value("${netty.server.port}")
  private int serverPort;
  
  @Autowired
  private SerializerFactory serializerFactory;
}
