package org.example.rpc.interceptor.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.rpc.common.circuit.CircuitBreakerState;
import org.example.rpc.interceptor.RpcInterceptor;
import org.example.rpc.protocol.model.RpcRequest;
import org.example.rpc.protocol.model.RpcResponse;
import org.springframework.stereotype.Component;

/**
 * Logging interceptor.
 *
 * <p>Print the request processing log.
 *
 * @author Kunhua Huang
 */
@Slf4j
@Component
public class LoggingInterceptor implements RpcInterceptor {

  @Override
  public boolean preHandle(RpcRequest request) {
    log.debug("Processing request: [{}], method: [{}], parameters: [{}]",
        request.getSequence(),
        request.getMethodName(),
        request.getParameters());
    return true;
  }

  @Override
  public void postHandle(RpcRequest request, RpcResponse response, CircuitBreakerState state) {
    if (response.getThrowable() != null) {
      log.error("Request processing error: [{}], error message: [{}]",
          request.getSequence(),
          response.getThrowable().getMessage());
    } else {
      log.debug("Request completed: [{}], result: [{}]",
          request.getSequence(),
          response.getResult());
    }
  }

  @Override
  public void afterCompletion(RpcRequest request, RpcResponse response, Throwable ex) {
    if (ex != null) {
      log.error("Request final error: [{}], error message: [{}]",
          request.getSequence(),
          ex.getMessage());
    }
  }
}