package org.example.rpc.core.interceptor.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.rpc.core.interceptor.RpcInterceptor;
import org.example.rpc.core.model.RpcRequest;
import org.example.rpc.core.model.RpcResponse;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingInterceptor implements RpcInterceptor {

  @Override
  public boolean preHandle(RpcRequest request) {
    log.info("Processing request: [{}], method: [{}], parameters: [{}]",
        request.getSequence(),
        request.getMethodName(),
        request.getParameters());
    return true;
  }

  @Override
  public void postHandle(RpcRequest request, RpcResponse response) {
    if (response.getThrowable() != null) {
      log.error("Request processing error: [{}], error message: [{}]",
          request.getSequence(),
          response.getThrowable().getMessage());
    } else {
      log.info("Request completed: [{}], result: [{}]",
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