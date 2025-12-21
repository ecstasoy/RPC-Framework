package io.ecstasoy.rpc.processor;

import lombok.extern.slf4j.Slf4j;
import io.ecstasoy.rpc.interceptor.InterceptorChainManager;
import io.ecstasoy.rpc.protocol.model.RpcRequest;
import io.ecstasoy.rpc.protocol.model.RpcResponse;
import io.ecstasoy.rpc.router.api.Router;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
/**
 * RPC request processor.
 */
@Slf4j
@Component
public class RpcRequestProcessor {

  private final Router router;
  private final InterceptorChainManager interceptorChainManager;
  private static final Map<String, Object> SERVICE_MAP = new ConcurrentHashMap<>();

  /**
   * Constructor.
   *
   * @param router router
   * @param interceptorChainManager interceptor chain manager
   */
  public RpcRequestProcessor(Router router, InterceptorChainManager interceptorChainManager) {
    this.router = router;
    this.interceptorChainManager = interceptorChainManager;
  }

  /**
   * Add service to service map.
   *
   * @param serviceName service name
   * @param service service
   */
  public static void addService(String serviceName, Object service) {
    SERVICE_MAP.put(serviceName, service);
  }

  /**
   * Get service by service name.
   *
   * @param serviceName service name
   * @return service
   */
  public static Object getService(String serviceName) {
    return SERVICE_MAP.get(serviceName);
  }

  /**
   * Process RPC request.
   *
   * @param request RPC request
   * @return RPC response
   */
  public CompletableFuture<RpcResponse> processRequest(RpcRequest request) {
    return CompletableFuture.supplyAsync(() -> {
      RpcResponse response = new RpcResponse();
      response.setSequence(request.getSequence());

      try {
        // Pre-handle phase
        if (!interceptorChainManager.applyPreHandle(request)) {
          return response;
        }

        // Process the request
        Object result = doProcess(request);
        response.setResult(result);

        // Post-handle phase
        interceptorChainManager.applyPostHandle(request, response);

      } catch (Exception e) {
        log.error("Error processing request", e);
        response.setThrowable(e.getCause() != null ? e.getCause() : e);
        interceptorChainManager.applyPostHandle(request, response);
      } finally {
        // Completion phase
        interceptorChainManager.applyAfterCompletion(request, response, 
            response.getThrowable());
      }

      return response;
    });
  }

  private Object doProcess(RpcRequest request) throws Exception {
    Object service = getService(request.getClassName());
    if (service == null) {
      throw new IllegalStateException("Service not found: " + request.getClassName());
    }

    Method method = router.route(service.getClass(), request.getMethodName(), 
        request.getHttpMethod());
    if (method == null) {
      throw new IllegalStateException("Method not found: " + request.getMethodName());
    }

    Object[] args = router.resolveParameters(method, request);
    Object result = method.invoke(service, args);

    if (result instanceof CompletableFuture) {
      result = ((CompletableFuture<?>) result).get();
    }

    return result;
  }

  /**
   * Remove service from service map.
   *
   * @param serviceName service name
   */
  public static void remove(String serviceName) {
    SERVICE_MAP.remove(serviceName);
  }
}
