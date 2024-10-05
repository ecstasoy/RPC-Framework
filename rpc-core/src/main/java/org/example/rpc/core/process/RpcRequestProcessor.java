package org.example.rpc.core.process;

import org.example.rpc.core.model.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
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
   * @param rpcRequest RPC request
   * @return result
   */
  public static Object process(RpcRequest rpcRequest) {
    try {
      final String className = rpcRequest.getClassName();
      final Object service = SERVICE_MAP.get(className);
      if (service == null) {
        throw new RuntimeException(String.format("Service not found: %s", className));
      }

      final Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
      final Object invoke = method.invoke(service, rpcRequest.getParameters());
      log.debug("Service [{}.{}] invoked.", className, rpcRequest.getMethodName());
      return invoke;
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      log.error("Fail to process request.", e);
      return null;
    }
  }
}
