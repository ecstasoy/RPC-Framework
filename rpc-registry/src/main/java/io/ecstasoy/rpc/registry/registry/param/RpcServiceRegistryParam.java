package io.ecstasoy.rpc.registry.registry.param;

import lombok.Data;

/**
 * Parameters for service registration.
 */
@Data
public class RpcServiceRegistryParam {

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
   * RPC bean.
   */
  private Object rpcBean;

  /**
   * Instance ID.
   */
  private String instanceId;
}
