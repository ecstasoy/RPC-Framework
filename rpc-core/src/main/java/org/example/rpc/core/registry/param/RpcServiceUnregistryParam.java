package org.example.rpc.core.registry.param;

import lombok.Data;

@Data
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

}
