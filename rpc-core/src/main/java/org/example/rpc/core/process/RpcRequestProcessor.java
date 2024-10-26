package org.example.rpc.core.process;

import org.example.rpc.core.model.RpcRequest;
import org.example.rpc.core.model.RpcResponse;
import org.example.rpc.core.annotations.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RpcRequestProcessor {

  private static final Map<String, Object> SERVICE_MAP = new ConcurrentHashMap<>();

  public static void addService(String serviceName, Object service) {
    SERVICE_MAP.put(serviceName, service);
  }

  public static Object getService(String serviceName) {
    return SERVICE_MAP.get(serviceName);
  }

  public static CompletableFuture<RpcResponse> processRequest(RpcRequest request) {
    return CompletableFuture.supplyAsync(() -> {
      RpcResponse response = new RpcResponse();
      response.setSequence(request.getSequence());

      try {
        Object service = getService(request.getClassName());
        if (service == null) {
          throw new IllegalStateException("Service not found: " + request.getClassName());
        }

        Method method = findMethod(service.getClass(), request.getMethodName(), request.getHttpMethod());
        if (method == null) {
          throw new IllegalStateException("Method not found: " + request.getMethodName());
        }

        Object[] args = extractParameters(method, request);
        Object result = method.invoke(service, args);

        if (result instanceof CompletableFuture) {
          result = ((CompletableFuture<?>) result).get();
        }

        response.setResult(result);
      } catch (Exception e) {
        log.error("Error processing request", e);
        Throwable cause = e.getCause() != null ? e.getCause() : e;
        response.setThrowable(cause);
      }

      return response;
    });
  }

  private static Method findMethod(Class<?> serviceClass, String methodName, String httpMethod) {
    for (Method method : serviceClass.getDeclaredMethods()) {
      if (method.getName().equals(methodName) && hasHttpMethodAnnotation(method, httpMethod)) {
        return method;
      }
    }
    return null;
  }

  private static boolean hasHttpMethodAnnotation(Method method, String httpMethod) {
    switch (httpMethod) {
      case "GET":
        return method.isAnnotationPresent(GET.class);
      case "POST":
        return method.isAnnotationPresent(POST.class);
      case "PUT":
        return method.isAnnotationPresent(PUT.class);
      case "DELETE":
        return method.isAnnotationPresent(DELETE.class);
      default:
        return false;
    }
  }

  private static Object[] extractParameters(Method method, RpcRequest request) {
    Parameter[] parameters = method.getParameters();
    Object[] args = new Object[parameters.length];
    Map<String, Object> paramMap = request.getParameters();
    Map<String, String> queryParams = request.getQueryParams();

    for (int i = 0; i < parameters.length; i++) {
      Parameter param = parameters[i];
      if (param.isAnnotationPresent(Path.class)) {
        String pathParamName = param.getAnnotation(Path.class).value();
        args[i] = request.getPath().split("/")[1]; // 简单处理，假设路径参数总是在第二个位置
      } else if (param.isAnnotationPresent(Query.class)) {
        String queryParamName = param.getAnnotation(Query.class).value();
        args[i] = queryParams.get(queryParamName);
      } else if (param.isAnnotationPresent(Body.class)) {
        args[i] = paramMap.get("body");
      } else {
        args[i] = paramMap.get(param.getName());
      }
    }
    return args;
  }

  public static void remove(String serviceName) {
    SERVICE_MAP.remove(serviceName);
  }
}