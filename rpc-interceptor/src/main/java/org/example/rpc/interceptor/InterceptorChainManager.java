package org.example.rpc.interceptor;

import org.example.rpc.interceptor.impl.LoggingInterceptor;
import org.example.rpc.interceptor.impl.PerformanceInterceptor;
import org.example.rpc.protocol.model.RpcRequest;
import org.example.rpc.protocol.model.RpcResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;

/**
 * Server-side interceptor chain manager
 *
 * <p>Manage the interceptor chain and provide methods to apply interceptors.
 * It is responsible for initializing the interceptors and applying them in order.
 *
 * @see RpcInterceptor
 * @see PerformanceInterceptor
 * @see LoggingInterceptor
 * @author Kunhua Huang
 */
@Component
public class InterceptorChainManager {
  private final List<RpcInterceptor> interceptors = new ArrayList<>();

  private final PerformanceInterceptor performanceInterceptor;
  private final LoggingInterceptor loggingInterceptor;

  /**
   * Constructor
   *
   * @param performanceInterceptor performance interceptor
   * @param loggingInterceptor logging interceptor
   */
  public InterceptorChainManager(
      PerformanceInterceptor performanceInterceptor,
      LoggingInterceptor loggingInterceptor) {
    this.performanceInterceptor = performanceInterceptor;
    this.loggingInterceptor = loggingInterceptor;
  }

  /**
   * Initialize the interceptors
   *
   * <p>Initialize the interceptors and add them to the interceptor chain.
   */
  @PostConstruct
  public void init() {
    // Add interceptors in order
    interceptors.add(performanceInterceptor);
    interceptors.add(loggingInterceptor);
  }

  /**
   * Apply preHandle method of interceptors
   *
   * <p>Apply the preHandle method of each interceptor in the interceptor chain.
   * If any interceptor returns false, the method will return false.
   *
   * @param request RPC request
   * @return true if all interceptors return true, false otherwise
   */
  public boolean applyPreHandle(RpcRequest request) {
    for (RpcInterceptor interceptor : interceptors) {
      if (interceptor.preHandle(request)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Apply postHandle method of interceptors
   *
   * <p>Apply the postHandle method of each interceptor in the interceptor chain.
   *
   * @param request RPC request
   * @param response RPC response
   */
  public void applyPostHandle(RpcRequest request, RpcResponse response) {
    for (int i = interceptors.size() - 1; i >= 0; i--) {
      interceptors.get(i).postHandle(request, response, null);
    }
  }

  /**
   * Apply afterCompletion method of interceptors
   *
   * <p>Apply the afterCompletion method of each interceptor in the interceptor chain.
   *
   * @param request RPC request
   * @param response RPC response
   * @param ex exception
   */
  public void applyAfterCompletion(RpcRequest request, RpcResponse response, Throwable ex) {
    for (int i = interceptors.size() - 1; i >= 0; i--) {
      interceptors.get(i).afterCompletion(request, response, ex);
    }
  }
}