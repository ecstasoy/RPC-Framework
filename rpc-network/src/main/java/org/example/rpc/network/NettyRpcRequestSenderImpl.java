package org.example.rpc.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.rpc.common.circuit.CircuitBreaker;
import org.example.rpc.common.circuit.CircuitBreakerProperties;
import org.example.rpc.common.exception.RpcException;
import org.example.rpc.registry.discovery.api.RpcServiceDiscovery;
import org.example.rpc.interceptor.ClientInterceptorChainManager;
import org.example.rpc.protocol.model.RpcRequest;
import org.example.rpc.protocol.model.RpcResponse;
import org.example.rpc.transport.client.RequestFutureManager;
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
    String serviceName = rpcRequest.getClassName();

    try {

      String serviceInstance = rpcServiceDiscovery.getServiceInstance(serviceName);
      if (StringUtils.isBlank(serviceInstance)) {
        throw new RpcException("SERVICE_UNAVAILABLE", "Service instance not found for " + serviceName, 404);
      }

      String[] addressParts = serviceInstance.split("#");
      if (addressParts.length != 2) {
        throw new RpcException("INVALID_ADDRESS",
            "Invalid service address format: " + serviceInstance + " for " + serviceName, 500);
      }

      String[] hostPort = addressParts[1].split(":");
      if (hostPort.length != 2) {
        throw new RpcException("INVALID_ADDRESS",
            "Invalid host:port format: " + addressParts[1] + " for " + serviceName, 500);
      }

      InetSocketAddress address = new InetSocketAddress(
          hostPort[0],
          Integer.parseInt(hostPort[1])
      );

      Channel channel = ChannelManager.get(address);
      if (channel == null || !channel.isActive()) {
        log.warn("Channel for {} is null or inactive, trying to reconnect...", address);
        channel = ChannelManager.connect(address);
        if (channel == null) {
          throw new RpcException("CONNECTION_FAILED",
              "Failed to create channel to " + address, 503);
        }
      }

      CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();
      RequestFutureManager.addFuture(rpcRequest.getSequence(), resultFuture);

      channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) future -> {
        if (!future.isSuccess()) {
          log.error("[{}] Failed to send request", requestId, future.cause());
          handleException(rpcRequest, future.cause(), resultFuture);
        }
      });

      return resultFuture;

    } catch (Exception e) {
      log.error("[{}] Failed to process request", requestId, e);
      CompletableFuture<RpcResponse> errorFuture = new CompletableFuture<>();
      handleException(rpcRequest, e, errorFuture);
      return errorFuture;
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

  /**
   * Reset the circuit breaker of the specified service.
   *
   * @param serviceName service name
   */
  public void resetCircuitBreaker(String serviceName) {
    CircuitBreaker breaker = circuitBreakers.get(serviceName);
    if (breaker != null) {
      breaker.reset();
      log.info("Reset circuit breaker for service: {}", serviceName);
    }
  }

  /**
   * Reset all circuit breakers.
   */
  public void resetAllCircuitBreakers() {
    circuitBreakers.forEach((service, breaker) -> {
      breaker.reset();
      log.info("Reset circuit breaker for service: {}", service);
    });
  }
}