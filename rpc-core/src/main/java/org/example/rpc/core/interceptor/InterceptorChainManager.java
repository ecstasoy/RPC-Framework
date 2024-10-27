package org.example.rpc.core.interceptor;

import org.example.rpc.core.interceptor.impl.LoggingInterceptor;
import org.example.rpc.core.interceptor.impl.PerformanceInterceptor;
import org.example.rpc.core.model.RpcRequest;
import org.example.rpc.core.model.RpcResponse;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
public class InterceptorChainManager {
  private final List<RpcInterceptor> interceptors = new ArrayList<>();

  private final PerformanceInterceptor performanceInterceptor;
  private final LoggingInterceptor loggingInterceptor;

  public InterceptorChainManager(
      PerformanceInterceptor performanceInterceptor,
      LoggingInterceptor loggingInterceptor) {
    this.performanceInterceptor = performanceInterceptor;
    this.loggingInterceptor = loggingInterceptor;
  }

  @PostConstruct
  public void init() {
    // Add interceptors in order
    interceptors.add(performanceInterceptor);
    interceptors.add(loggingInterceptor);
  }

  public boolean applyPreHandle(RpcRequest request) {
    for (RpcInterceptor interceptor : interceptors) {
      if (!interceptor.preHandle(request)) {
        return false;
      }
    }
    return true;
  }

  public void applyPostHandle(RpcRequest request, RpcResponse response) {
    for (int i = interceptors.size() - 1; i >= 0; i--) {
      interceptors.get(i).postHandle(request, response);
    }
  }

  public void applyAfterCompletion(RpcRequest request, RpcResponse response, Throwable ex) {
    for (int i = interceptors.size() - 1; i >= 0; i--) {
      interceptors.get(i).afterCompletion(request, response, ex);
    }
  }
}