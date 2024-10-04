package org.example.rpc.core.network;

import org.example.rpc.core.model.RpcResponse;
import org.example.rpc.core.model.RpcRequest;

/**
 * RPC request sender.
 */
public interface RpcRequestSender {

  /**
   * Send RPC request.
   *
   * @param rpcRequest RPC request
   * @return RPC response
   */
  RpcResponse sendRpcRequest(RpcRequest rpcRequest);
}
