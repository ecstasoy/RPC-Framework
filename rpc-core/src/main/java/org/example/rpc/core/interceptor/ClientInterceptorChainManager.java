package org.example.rpc.core.interceptor;

import org.example.rpc.core.common.circuit.CircuitBreakerState;
import org.example.rpc.core.interceptor.RpcInterceptor;
import org.example.rpc.core.interceptor.impl.ClientCircuitBreakerInterceptor;
import org.example.rpc.core.interceptor.impl.LoggingInterceptor;
import org.example.rpc.core.model.RpcRequest;
import org.example.rpc.core.model.RpcResponse;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component("clientInterceptorChainManager")
public class ClientInterceptorChainManager {
  private final List<RpcInterceptor> interceptors = new ArrayList<>();

  private final ClientCircuitBreakerInterceptor circuitBreakerInterceptor;
  private final LoggingInterceptor loggingInterceptor;

  public ClientInterceptorChainManager(
      ClientCircuitBreakerInterceptor circuitBreakerInterceptor,
      LoggingInterceptor loggingInterceptor) {
    this.circuitBreakerInterceptor = circuitBreakerInterceptor;
    this.loggingInterceptor = loggingInterceptor;
  }

  @PostConstruct
  public void init() {
    // 添加拦截器，注意顺序
    interceptors.add(loggingInterceptor);        // 首先记录日志
    interceptors.add(circuitBreakerInterceptor); // 然后记录熔断指标
  }

  public boolean applyPreHandle(RpcRequest request) {
    for (RpcInterceptor interceptor : interceptors) {
      if (!interceptor.preHandle(request)) {
        return false;
      }
    }
    return true;
  }

  public void applyPostHandle(RpcRequest request, RpcResponse response, CircuitBreakerState state) {
    // 反向执行 post 处理
    for (int i = interceptors.size() - 1; i >= 0; i--) {
      interceptors.get(i).postHandle(request, response, state);
    }
  }

  public void applyAfterCompletion(RpcRequest request, RpcResponse response, Throwable ex) {
    // 反向执行完成后处理
    for (int i = interceptors.size() - 1; i >= 0; i--) {
      interceptors.get(i).afterCompletion(request, response, ex);
    }
  }
}