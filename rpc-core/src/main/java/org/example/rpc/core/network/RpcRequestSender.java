package org.example.rpc.core.network;

import org.example.rpc.core.model.RpcResponse;
import org.example.rpc.core.model.RpcRequest;

import java.util.concurrent.CompletableFuture;

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
  CompletableFuture<RpcResponse> sendRpcRequest(RpcRequest rpcRequest);
}
