package org.example.rpc.core.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.rpc.core.client.RequestFutureManager;
import org.example.rpc.core.discovery.RpcServiceDiscovery;
import org.example.rpc.core.model.RpcRequest;
import org.example.rpc.core.model.RpcResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * Netty RPC request sender implementation.
 */
@Service
@Slf4j
public class NettyRpcRequestSenderImpl implements RpcRequestSender {

  @Autowired
  private RpcServiceDiscovery rpcServiceDiscovery;

  @SneakyThrows
  @Override
  public CompletableFuture<RpcResponse> sendRpcRequest(RpcRequest rpcRequest) {
    CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();

    int maxRetries = 3;
    for (int i = 0; i < maxRetries; i++) {
      try {
        return doSendRpcRequest(rpcRequest);
      } catch (Exception e) {
        log.warn("Failed to send RPC request, attempt {} of {}", i + 1, maxRetries, e);
        if (i == maxRetries - 1) {
          resultFuture.completeExceptionally(new RuntimeException("Failed to send RPC request after " + maxRetries + " attempts", e));
          return resultFuture;
        }
        try {
          Thread.sleep(1000);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          resultFuture.completeExceptionally(new RuntimeException("Interrupted while retrying to send RPC request", ie));
          return resultFuture;
        }
      }
    }
    resultFuture.completeExceptionally(new RuntimeException("Unexpected error in sendRpcRequest"));
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
