package org.example.rpc.core.process;

import org.example.rpc.core.model.RpcRequest;
import org.example.rpc.core.model.RpcResponse;
import org.example.rpc.core.annotations.RpcMethod;
import org.example.rpc.core.annotations.Param;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class of RPC request processor.
 */
@Slf4j
public class RpcRequestProcessor {

  private static final Map<String, Object> SERVICE_MAP = new ConcurrentHashMap<>();

  /**
   * Add service.
   *
   * @param serviceName service name
   * @param service     service
   */
  public static void addService(String serviceName, Object service) {
    SERVICE_MAP.put(serviceName, service);
  }

  /**
   * Get service.
   *
   * @param serviceName service name
   * @return service
   */
  public static Object getService(String serviceName) {
    return SERVICE_MAP.get(serviceName);
  }

  /**
   * Remove service.
   *
   * @param serviceName service name
   */
  public static void remove(String serviceName) {
    SERVICE_MAP.remove(serviceName);
  }

  /**
   * Process RPC request.
   *
   * @param request RPC request
   * @return result
   */
  public static CompletableFuture<RpcResponse> processRequest(RpcRequest request) {
    return CompletableFuture.supplyAsync(() -> {
      RpcResponse response = new RpcResponse();
      response.setSequence(request.getSequence());

      try {
        Object service = SERVICE_MAP.get(request.getClassName());
        log.info("Found service: {}", service);
        if (service == null) {
          throw new IllegalStateException("Service not found: " + request.getClassName());
        }

        Method method = findMethod(service.getClass(), request.getMethodName());
        if (method == null) {
          throw new IllegalStateException("Method not found: " + request.getMethodName());
        }

        Object[] args = extractParameters(method, request.getParameters());
        Object result = method.invoke(service, args);
        
        if (result instanceof CompletableFuture) {
          return ((CompletableFuture<?>) result).thenApply(r -> {
            response.setResult(r);
            return response;
          }).exceptionally(ex -> {
            response.setThrowable(ex);
            return response;
          }).get();
        } else {
          response.setResult(result);
        }
      } catch (Exception e) {
        log.error("Error processing request", e);
        response.setThrowable(e);
      }

      return response;
    });
  }

  private static Method findMethod(Class<?> serviceClass, String methodName) {
    log.info("Searching for method: {} in class: {}", methodName, serviceClass.getName());
    for (Method method : serviceClass.getDeclaredMethods()) {
      log.info("Checking method: {}", method.getName());
      if (method.isAnnotationPresent(RpcMethod.class) && method.getName().equals(methodName)) {
        return method;
      }
    }
    return null;
  }

  private static Object[] extractParameters(Method method, Map<String, Object> parameterMap) {
    Parameter[] parameters = method.getParameters();
    Object[] args = new Object[parameters.length];
    for (int i = 0; i < parameters.length; i++) {
      Parameter param = parameters[i];
      String paramName = param.getName();
      if (param.isAnnotationPresent(Param.class)) {
        paramName = param.getAnnotation(Param.class).value();
      }
      args[i] = parameterMap.get(paramName);
    }
    return args;
  }
}
