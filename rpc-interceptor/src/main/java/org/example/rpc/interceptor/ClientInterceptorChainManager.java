package org.example.rpc.interceptor;

import org.example.rpc.common.circuit.CircuitBreakerState;
import org.example.rpc.interceptor.impl.ClientCircuitBreakerInterceptor;
import org.example.rpc.interceptor.impl.LoggingInterceptor;
import org.example.rpc.protocol.model.RpcRequest;
import org.example.rpc.protocol.model.RpcResponse;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Client-side Interceptor Chain Manager.
 *
 * <p>Manage the interceptor chain for client-side RPC requests.
 * It applies the preHandle, postHandle, and afterCompletion methods of each interceptor in the chain.
 *
 * @see RpcInterceptor
 * @see ClientCircuitBreakerInterceptor
 * @see LoggingInterceptor
 * @author Kunhua Huang
 */
@Component("clientInterceptorChainManager")
public class ClientInterceptorChainManager {
  private final List<RpcInterceptor> interceptors = new ArrayList<>();

  private final ClientCircuitBreakerInterceptor circuitBreakerInterceptor;
  private final LoggingInterceptor loggingInterceptor;

  /**
   * Constructor.
   *
   * @param circuitBreakerInterceptor Circuit Breaker Interceptor
   * @param loggingInterceptor Logging Interceptor
   */
  public ClientInterceptorChainManager(
      ClientCircuitBreakerInterceptor circuitBreakerInterceptor,
      LoggingInterceptor loggingInterceptor) {
    this.circuitBreakerInterceptor = circuitBreakerInterceptor;
    this.loggingInterceptor = loggingInterceptor;
  }

  /**
   * Initialize the interceptor chain.
   */
  @PostConstruct
  public void init() {
    // Add interceptors to the chain, in the order of execution.
    interceptors.add(loggingInterceptor);
    interceptors.add(circuitBreakerInterceptor);
  }

  /**
   * Apply the preHandle method of each interceptor in the chain.
   *
   * @param request RPC request
   * @return true if all interceptors return true, false otherwise
   */
  public boolean applyPreHandle(RpcRequest request) {
    for (RpcInterceptor interceptor : interceptors) {
      if (!interceptor.preHandle(request)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Apply the postHandle method of each interceptor in the chain.
   *
   * @param request RPC request
   * @param response RPC response
   * @param state Circuit Breaker State
   */
  public void applyPostHandle(RpcRequest request, RpcResponse response, CircuitBreakerState state) {
    for (int i = interceptors.size() - 1; i >= 0; i--) {
      interceptors.get(i).postHandle(request, response, state);
    }
  }

  /**
   * Apply the afterCompletion method of each interceptor in the chain.
   *
   * @param request RPC request
   * @param response RPC response
   * @param ex Exception
   */
  public void applyAfterCompletion(RpcRequest request, RpcResponse response, Throwable ex) {
    for (int i = interceptors.size() - 1; i >= 0; i--) {
      interceptors.get(i).afterCompletion(request, response, ex);
    }
  }
}