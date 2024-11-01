package org.example.rpc.network;

import org.example.rpc.protocol.model.RpcResponse;
import org.example.rpc.protocol.model.RpcRequest;

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
