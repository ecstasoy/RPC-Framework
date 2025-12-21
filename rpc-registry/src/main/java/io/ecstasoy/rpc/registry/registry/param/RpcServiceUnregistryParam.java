package io.ecstasoy.rpc.registry.registry.param;

import lombok.Builder;
import lombok.Data;

/**
 * Parameters for service unregistration.
 */
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
