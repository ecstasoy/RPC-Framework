package org.example.rpc.registry.registry.param;

import lombok.Data;

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
