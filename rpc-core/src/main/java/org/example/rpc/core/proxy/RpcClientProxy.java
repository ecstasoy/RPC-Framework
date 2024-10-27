package org.example.rpc.core.proxy;

import lombok.extern.slf4j.Slf4j;
import org.example.rpc.core.common.annotations.*;
import org.example.rpc.core.model.RpcRequest;
import org.example.rpc.core.model.RpcResponse;
import org.example.rpc.core.network.RpcRequestSender;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Class of RPC client proxy, implemented by JDK dynamic proxy.
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {

  private final RpcRequestSender requestSender;

  /**
   * Constructor.
   *
   * @param requestSender RPC request sender
   */
  public RpcClientProxy(RpcRequestSender requestSender) {
    this.requestSender = requestSender;
  }

  @SuppressWarnings("unchecked")
  public <T> T getProxy(Class<T> clazz) {
    return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (Object.class.equals(method.getDeclaringClass())) {
      return method.invoke(this, args);
    }

    String httpMethod = null;
    String path = "";

    if (method.isAnnotationPresent(GET.class)) {
      httpMethod = "GET";
      path = method.getAnnotation(GET.class).value();
    } else if (method.isAnnotationPresent(POST.class)) {
      httpMethod = "POST";
      path = method.getAnnotation(POST.class).value();
    } else if (method.isAnnotationPresent(PUT.class)) {
      httpMethod = "PUT";
      path = method.getAnnotation(PUT.class).value();
    } else if (method.isAnnotationPresent(DELETE.class)) {
      httpMethod = "DELETE";
      path = method.getAnnotation(DELETE.class).value();
    }

    if (httpMethod == null) {
      throw new IllegalStateException("HTTP method annotation is missing");
    }

    RpcRequest rpcRequest = buildRequest(method, args, httpMethod, path);
    CompletableFuture<RpcResponse> responseFuture = requestSender.sendRpcRequest(rpcRequest);

    if (CompletableFuture.class.isAssignableFrom(method.getReturnType())) {
      return responseFuture.thenApply(this::handleResponse);
    } else {
      return handleResponse(responseFuture.get());
    }
  }

  private RpcRequest buildRequest(Method method, Object[] args, String httpMethod, String path) {
    String methodName = method.getName();
    Parameter[] parameters = method.getParameters();
    Map<String, Object> paramMap = new HashMap<>();
    Map<String, String> queryParams = new HashMap<>();

    for (int i = 0; i < parameters.length; i++) {
      Parameter param = parameters[i];
      if (param.isAnnotationPresent(Path.class)) {
        String pathParamName = param.getAnnotation(Path.class).value();
        path = path.replace("{" + pathParamName + "}", args[i].toString());
      } else if (param.isAnnotationPresent(Query.class)) {
        String queryParamName = param.getAnnotation(Query.class).value();
        queryParams.put(queryParamName, args[i].toString());
      } else if (param.isAnnotationPresent(Body.class)) {
        paramMap.put("body", args[i]);
      } else {
        paramMap.put("param" + i, args[i]);
      }
    }

    return RpcRequest.builder()
        .className(method.getDeclaringClass().getName())
        .methodName(methodName)
        .parameters(paramMap)
        .parameterTypes(method.getParameterTypes())
        .sequence(UUID.randomUUID().toString())
        .httpMethod(httpMethod)
        .path(path)
        .queryParams(queryParams)
        .build();
  }

  private Object handleResponse(RpcResponse rpcResponse) {
    if (rpcResponse.getThrowable() != null) {
      if (rpcResponse.getThrowable() instanceof RuntimeException) {
        throw (RuntimeException) rpcResponse.getThrowable();
      }
      throw new RuntimeException(rpcResponse.getThrowable());
    }
    return rpcResponse.getResult();
  }
}
