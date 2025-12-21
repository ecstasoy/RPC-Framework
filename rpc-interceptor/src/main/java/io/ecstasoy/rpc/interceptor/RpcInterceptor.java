package io.ecstasoy.rpc.interceptor;

import io.ecstasoy.rpc.common.circuit.CircuitBreakerState;
import io.ecstasoy.rpc.protocol.model.RpcRequest;
import io.ecstasoy.rpc.protocol.model.RpcResponse;

/**
 * RPC interceptor
 *
 * <p>Interceptors are used to intercept RPC requests and responses.
 * They can be used to perform operations before and after the RPC request is processed.
 * <p>Here are some common use cases:
 *
 * @author Kunhua Huang
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