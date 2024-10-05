package org.example.rpc.core.server;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Properties of Netty server.
 */
@Data
@Component
public class NettyServerProperties {

  @Value("${netty.server.port:50001}")
  private int serverPort;
}
