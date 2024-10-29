package org.example.rpc.core.interceptor;

import org.example.rpc.core.common.circuit.CircuitBreakerState;
import org.example.rpc.core.model.RpcRequest;
import org.example.rpc.core.model.RpcResponse;

/**
 * RPC interceptor
 */
public interface RpcInterceptor {
  /**
   * Pre-process the RPC request
   * @param request RPC request
   * @return true if the request is valid, otherwise false
   */
  boolean preHandle(RpcRequest request);

  /**
   * Post-process the RPC request
   * @param request RPC request
   * @param response RPC response
   */
  void postHandle(RpcRequest request, RpcResponse response, CircuitBreakerState state);

  /**
   * After completion of the RPC request
   * @param request RPC request
   * @param response RPC response
   * @param ex exception
   */
  void afterCompletion(RpcRequest request, RpcResponse response, Throwable ex);
}