package org.example.rpc.core.client;

import lombok.extern.slf4j.Slf4j;
import org.example.rpc.core.annotations.RpcMethod;
import org.example.rpc.core.annotations.Param;
import org.example.rpc.core.model.RpcRequest;
import org.example.rpc.core.model.RpcResponse;
import org.example.rpc.core.network.RpcRequestSender;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Class of RPC client proxy.
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

    RpcMethod rpcMethod = method.getAnnotation(RpcMethod.class);
    if (rpcMethod == null) {
      throw new IllegalStateException("RPC method must be annotated with @RpcMethod");
    }

    RpcRequest rpcRequest = buildRequest(method, args);
    CompletableFuture<RpcResponse> responseFuture = requestSender.sendRpcRequest(rpcRequest);

    if (CompletableFuture.class.isAssignableFrom(method.getReturnType())) {
      return responseFuture.thenApply(this::handleResponse);
    } else {
      return handleResponse(responseFuture.get());
    }
  }

  private RpcRequest buildRequest(Method method, Object[] args) {
    String methodName = method.getName();
    Parameter[] parameters = method.getParameters();
    Map<String, Object> paramMap = new HashMap<>();

    for (int i = 0; i < parameters.length; i++) {
      Param param = parameters[i].getAnnotation(Param.class);
      if (param != null) {
        paramMap.put(param.value(), args[i]);
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
        .build();
  }

  private Object handleResponse(RpcResponse rpcResponse) {
    if (rpcResponse.getThrowable() != null) {
      throw new RuntimeException(rpcResponse.getThrowable());
    }
    return rpcResponse.getResult();
  }
}
