package org.example.rpc.core.client;

import org.example.rpc.core.model.RpcResponse;
import org.example.rpc.core.network.RpcRequestSender;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Class of RPC client proxy.
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {

  public <T> T getProxy(Class<T> clazz) {
    return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // Send RPC request to server, and get response.

  }
}