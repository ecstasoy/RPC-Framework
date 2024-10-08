package org.example.rpc.core.client;

import org.example.rpc.core.model.RpcRequest;
import org.example.rpc.core.model.RpcResponse;
import org.example.rpc.core.network.RpcRequestSender;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.UUID;

/**
 * Class of RPC client proxy.
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {

  private final RpcRequestSender rpcRequestSender;

  /**
   * Constructor.
   *
   * @param rpcRequestSender RPC request sender
   */
  public RpcClientProxy(RpcRequestSender rpcRequestSender) {
    this.rpcRequestSender = rpcRequestSender;
  }

  /**
   * Get proxy.
   *
   * @param clazz class
   * @return proxy
   */
  public <T> T getProxy(Class<T> clazz) {
    return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    RpcRequest rpcRequest = new RpcRequest();
    rpcRequest.setSequence(UUID.randomUUID().toString());
    rpcRequest.setClassName(method.getDeclaringClass().getName());
    rpcRequest.setMethodName(method.getName());
    rpcRequest.setParameterTypes(method.getParameterTypes());
    rpcRequest.setParameters(args);

    log.info("Invoking method: {}, on proxy: {} with arguments: {}",
        method.getName(), proxy.getClass().getName(), Arrays.toString(args));
    log.info("Sending RPC request: {}", rpcRequest);

    final RpcResponse rpcResponse = rpcRequestSender.sendRpcRequest(rpcRequest);

    log.info("Received RPC response: {}", rpcResponse);

    if (rpcResponse.getThrowable() != null) {
      throw rpcResponse.getThrowable();
    }

    return rpcResponse.getResult();
  }
}