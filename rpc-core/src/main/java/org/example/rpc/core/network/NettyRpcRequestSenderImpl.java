package org.example.rpc.core.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.rpc.core.common.circuit.CircuitBreaker;
import org.example.rpc.core.common.circuit.CircuitBreakerProperties;
import org.example.rpc.core.common.exception.RpcException;
import org.example.rpc.core.discovery.api.RpcServiceDiscovery;
import org.example.rpc.core.interceptor.ClientInterceptorChainManager;
import org.example.rpc.core.model.RpcRequest;
import org.example.rpc.core.model.RpcResponse;
import org.example.rpc.core.transport.client.RequestFutureManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Netty RPC request sender implementation.
 */
@Service
@Slf4j
public class NettyRpcRequestSenderImpl implements RpcRequestSender {

  private final ConcurrentHashMap<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();
  @Autowired
  private RpcServiceDiscovery rpcServiceDiscovery;
  @Autowired
  private CircuitBreakerProperties circuitBreakerProperties;
  @Autowired
  private ClientInterceptorChainManager clientInterceptorChainManager;

  private CircuitBreaker getCircuitBreaker(String serviceName) {
    return circuitBreakers.computeIfAbsent(serviceName,
        k -> new CircuitBreaker(circuitBreakerProperties));
  }

  @SneakyThrows
  @Override
  public CompletableFuture<RpcResponse> sendRpcRequest(RpcRequest rpcRequest) {
    clientInterceptorChainManager.applyPreHandle(rpcRequest);
    CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();
    String serviceName = rpcRequest.getClassName();
    CircuitBreaker circuitBreaker = getCircuitBreaker(serviceName);

    if (!circuitBreaker.allowRequest()) {
      handleException(rpcRequest,
          new RpcException("SERVICE_UNAVAILABLE",
              "Service is unavailable due to circuit breaker open: " + serviceName,
              503),
          resultFuture);
      return resultFuture;
    }

    try {
      CompletableFuture<RpcResponse> response = doSendRpcRequest(rpcRequest, circuitBreaker);
      response.whenComplete((result, throwable) -> {
        if (throwable != null) {
          circuitBreaker.recordFailure();
          resultFuture.completeExceptionally(throwable);
        } else {
          circuitBreaker.recordSuccess();
          resultFuture.complete(result);
        }
      });
    } catch (Exception e) {
      circuitBreaker.recordFailure();
      resultFuture.completeExceptionally(e);
    }

    return resultFuture;
  }

  private CompletableFuture<RpcResponse> doSendRpcRequest(RpcRequest rpcRequest, CircuitBreaker circuitBreaker) {
    String requestId = UUID.randomUUID().toString();
    CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();

    try {

      final String serviceInstance = rpcServiceDiscovery.getServiceInstance(rpcRequest.getClassName());
      if (StringUtils.isBlank(serviceInstance)) {
        log.error("[{}] 未找到服务实例", requestId);
        circuitBreaker.recordFailure();
        handleException(rpcRequest, new RpcException("SERVICE_UNAVAILABLE", "Service instance not found", 404), resultFuture);
        return resultFuture;
      }

      String[] split = serviceInstance.split(":");
      final Channel channel = ChannelManager.get(new InetSocketAddress(split[0], Integer.parseInt(split[1])));
      RequestFutureManager.addFuture(rpcRequest.getSequence(), resultFuture);

      resultFuture.whenComplete((response, throwable) -> {
        try {
          if (throwable != null) {
            handleException(rpcRequest, throwable, resultFuture);
          } else {
            clientInterceptorChainManager.applyPostHandle(rpcRequest, response, circuitBreaker.getState());
            clientInterceptorChainManager.applyAfterCompletion(rpcRequest, response, null);
          }
        } catch (Exception e) {
          log.error("[{}] 拦截器处理异常", requestId, e);
        }
      });

      channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) future -> {
        if (!future.isSuccess()) {
          log.error("[{}] 发送请求失败", requestId, future.cause());
          future.channel().close();
          handleException(rpcRequest, future.cause(), resultFuture);
        }
      });

      return resultFuture;

    } catch (Exception e) {
      log.error("[{}] 处理请求异常", requestId, e);
      handleException(rpcRequest, e, resultFuture);
      return resultFuture;
    }
  }

  private void handleException(RpcRequest rpcRequest, Throwable e, CompletableFuture<RpcResponse> resultFuture) {
    CircuitBreaker circuitBreaker = getCircuitBreaker(rpcRequest.getClassName());
    circuitBreaker.recordFailure();

    // 创建包含异常信息的 RpcResponse
    RpcResponse errorResponse = new RpcResponse();
    errorResponse.setSequence(rpcRequest.getSequence());
    if (e instanceof RpcException) {
      errorResponse.setThrowable(e);
    } else {
      errorResponse.setThrowable(new RpcException("INTERNAL_ERROR", e.getMessage(), 500));
    }

    try {
      clientInterceptorChainManager.applyPostHandle(rpcRequest, errorResponse, circuitBreaker.getState());
      clientInterceptorChainManager.applyAfterCompletion(rpcRequest, errorResponse, e);
    } catch (Exception ex) {
      log.error("Error in interceptor chain", ex);
    }

    resultFuture.completeExceptionally(errorResponse.getThrowable());
  }
}