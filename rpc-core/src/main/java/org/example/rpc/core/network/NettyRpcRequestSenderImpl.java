package org.example.rpc.core.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.rpc.core.common.circuit.CircuitBreaker;
import org.example.rpc.core.common.circuit.CircuitBreakerProperties;
import org.example.rpc.core.transport.client.RequestFutureManager;
import org.example.rpc.core.discovery.api.RpcServiceDiscovery;
import org.example.rpc.core.common.exception.RpcException;
import org.example.rpc.core.model.RpcRequest;
import org.example.rpc.core.model.RpcResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Netty RPC request sender implementation.
 */
@Service
@Slf4j
public class NettyRpcRequestSenderImpl implements RpcRequestSender {

  @Autowired
  private RpcServiceDiscovery rpcServiceDiscovery;

  @Autowired
  private CircuitBreakerProperties circuitBreakerProperties;

  private final ConcurrentHashMap<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();

  private CircuitBreaker getCircuitBreaker(String serviceName) {
    return circuitBreakers.computeIfAbsent(serviceName,
        k -> new CircuitBreaker(circuitBreakerProperties));
  }

  @SneakyThrows
  @Override
  public CompletableFuture<RpcResponse> sendRpcRequest(RpcRequest rpcRequest) {
    CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();

    String serviceName = rpcRequest.getClassName();
    CircuitBreaker circuitBreaker = getCircuitBreaker(serviceName);

    if (!circuitBreaker.allowRequest()) {
      resultFuture.completeExceptionally(
          new RpcException("SERVICE_UNAVAILABLE",
              "Service is unavailable due to circuit breaker open: " + serviceName, 
              503));
      return resultFuture;
    }

    try {
      CompletableFuture<RpcResponse> response = doSendRpcRequest(rpcRequest);
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

  private CompletableFuture<RpcResponse> doSendRpcRequest(RpcRequest rpcRequest) {
    CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();

    final String className = rpcRequest.getClassName();
    final String serviceInstance = rpcServiceDiscovery.getServiceInstance(className);
    if (StringUtils.isBlank(serviceInstance)) {
      resultFuture.completeExceptionally(new RuntimeException(className + " has no available service instance."));
      return resultFuture;
    }
    log.info("Discovered service instance [{}] for class [{}].", serviceInstance, className);

    String[] split = serviceInstance.split(":");
    final Channel channel = ChannelManager.get(new InetSocketAddress(split[0], Integer.parseInt(split[1])));
    if (channel == null || !channel.isActive()) {
      resultFuture.completeExceptionally(new IllegalStateException("No active channel for service instance: " + serviceInstance));
      return resultFuture;
    }
    log.info("Using channel [{}] for service instance [{}].", channel, serviceInstance);

    RequestFutureManager.addFuture(rpcRequest.getSequence(), resultFuture);

    channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) future -> {
      if (future.isSuccess()) {
        log.info("Send request [{}] to [{}].", rpcRequest.getSequence(), serviceInstance);
      } else {
        future.channel().close();
        resultFuture.completeExceptionally(future.cause());
        log.error("Send request [{}] to [{}] failed.", rpcRequest.getSequence(), serviceInstance);
      }
    });

    return resultFuture;
  }
}
