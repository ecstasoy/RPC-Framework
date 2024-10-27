package org.example.rpc.core.router.api;

import org.example.rpc.core.model.RpcRequest;
import java.lang.reflect.Method;

/**
 * Router class
 */
public interface Router {

  /**
   * Route the request to the target service.
   *
   * @param serviceClass service class
   * @param methodName   method name
   * @param httpMethod   http method
   * @return target method
   */
  Method route(Class<?> serviceClass, String methodName, String httpMethod);

  /**
   * Resolve the parameters of the method.
   *
   * @param method  method
   * @param request rpc request
   * @return parameters
   */
  Object[] resolveParameters(Method method, RpcRequest request);
}
