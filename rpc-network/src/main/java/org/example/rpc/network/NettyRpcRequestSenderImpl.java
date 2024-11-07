package org.example.rpc.network;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.rpc.common.circuit.CircuitBreaker;
import org.example.rpc.common.circuit.CircuitBreakerProperties;
import org.example.rpc.common.exception.RpcException;
import org.example.rpc.interceptor.ClientInterceptorChainManager;
import org.example.rpc.protocol.model.RpcRequest;
import org.example.rpc.protocol.model.RpcResponse;
import org.example.rpc.protocol.serialize.SerializerFactory;
import org.example.rpc.protocol.serialize.SerializerType;
import org.example.rpc.registry.discovery.api.RpcServiceDiscovery;
import org.example.rpc.transport.client.RequestFutureManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Netty RPC request sender implementation.
 */
@Service
@Slf4j
public class NettyRpcRequestSenderImpl implements RpcRequestSender {

  private final SerializerFactory serializerFactory;
  private final ChannelPool channelPool = new ChannelPool(500);
  private final Cache<String, CircuitBreaker> circuitBreakers = CacheBuilder.newBuilder()
      .expireAfterAccess(30, TimeUnit.MINUTES)
      .build();
  @Autowired
  private RpcServiceDiscovery rpcServiceDiscovery;
  @Autowired
  private CircuitBreakerProperties circuitBreakerProperties;
  @Autowired
  private ClientInterceptorChainManager clientInterceptorChainManager;
  @Autowired
  public NettyRpcRequestSenderImpl(SerializerFactory serializerFactory) {
    this.serializerFactory = serializerFactory;
  }

  private static InetSocketAddress getInetSocketAddress(String serviceInstance, String serviceName) {
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

    return new InetSocketAddress(
        hostPort[0],
        Integer.parseInt(hostPort[1])
    );
  }

  private CircuitBreaker getCircuitBreaker(String serviceName) throws ExecutionException {
    return circuitBreakers.get(serviceName, () -> new CircuitBreaker(circuitBreakerProperties));
  }

  @SneakyThrows
  @Override
  public CompletableFuture<RpcResponse> sendRpcRequest(RpcRequest rpcRequest) {
    rpcRequest.setSerializerType(SerializerType.fromType(serializerFactory.getDefaultType().getType()));
    clientInterceptorChainManager.applyPreHandle(rpcRequest);
    CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();
    String serviceName = rpcRequest.getClassName();
    CircuitBreaker circuitBreaker = getCircuitBreaker(serviceName);

    if (circuitBreaker.isCircuitbBreakerOpen()) {
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
          try {
            handleException(rpcRequest, throwable, resultFuture);
          } catch (ExecutionException e) {
            throw new RuntimeException(e);
          }
        } else {
          circuitBreaker.recordSuccess();
          try {
            clientInterceptorChainManager.applyPostHandle(rpcRequest, result, circuitBreaker.getState());
            clientInterceptorChainManager.applyAfterCompletion(rpcRequest, result, null);
          } catch (Exception ex) {
            log.error("Error in interceptor chain", ex);
          }
          resultFuture.complete(result);
        }
      });
    } catch (Exception e) {
      circuitBreaker.recordFailure();
      resultFuture.completeExceptionally(e);
    }

    return resultFuture;
  }

  private CompletableFuture<RpcResponse> doSendRpcRequest(RpcRequest rpcRequest, CircuitBreaker circuitBreaker) throws ExecutionException {
    String requestId = UUID.randomUUID().toString();
    String serviceName = rpcRequest.getClassName();

    try {

      String serviceInstance = rpcServiceDiscovery.getServiceInstance(serviceName);
      if (StringUtils.isBlank(serviceInstance)) {
        throw new RpcException("SERVICE_UNAVAILABLE", "Service instance not found for " + serviceName, 404);
      }

      InetSocketAddress address = getInetSocketAddress(serviceInstance, serviceName);

      Channel channel = channelPool.getChannel(address);
      if (channel == null) {
        throw new RpcException("CONNECTION_FAILED", "Failed to create or retrieve channel to " + address, 503);
      }

      CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();
      RequestFutureManager.addFuture(rpcRequest.getSequence(), resultFuture);

      channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) future -> {
        if (!future.isSuccess()) {
          log.error("[{}] Failed to send request", requestId, future.cause());
          handleException(rpcRequest, future.cause(), resultFuture);
        } else {
          log.debug("[{}] Successfully sent request to {}", requestId, address);
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

  private void handleException(RpcRequest rpcRequest, Throwable e, CompletableFuture<RpcResponse> resultFuture) throws ExecutionException {
    CircuitBreaker circuitBreaker = getCircuitBreaker(rpcRequest.getClassName());
    circuitBreaker.recordFailure();

    RpcResponse errorResponse = new RpcResponse();
    errorResponse.setSequence(rpcRequest.getSequence());
    errorResponse.setSerializerType(rpcRequest.getSerializerType());
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
    CircuitBreaker breaker = circuitBreakers.asMap().get(serviceName);
    if (breaker != null) {
      breaker.reset();
      log.info("Reset circuit breaker for service: {}", serviceName);
    }
  }

  /**
   * Reset all circuit breakers.
   */
  public void resetAllCircuitBreakers() {
    circuitBreakers.asMap().forEach((serviceName, breaker) -> {
      breaker.reset();
      log.info("Reset circuit breaker for service: {}", serviceName);
    });
  }
}