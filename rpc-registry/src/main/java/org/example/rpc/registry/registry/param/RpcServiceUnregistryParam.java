package org.example.rpc.registry.registry.param;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RpcServiceUnregistryParam {

  /**
   * Service name.
   */
  private String serviceName;

  /**
   * IP address.
   */
  private String ip;

  /**
   * Port.
   */
  private int port;

  /**
   * Instance ID.
   */
  private String instanceId;

}
