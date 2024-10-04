package org.example.rpc.core.network;

import org.example.rpc.core.model.RpcResponse;
import org.example.rpc.core.model.RpcRequest;
import org.example.rpc.core.client.RequestFutureManager;
import org.example.rpc.core.discovery.RpcServiceDiscovery;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class NettyRpcRequestSenderImpl implements RpcRequestSender {

  @Autowired
  private RpcServiceDiscovery rpcServiceDiscovery;

  @SneakyThrows
  @Override
  public RpcResponse sendRpcRequest(RpcRequest rpcRequest) {
    CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();

    final String className = rpcRequest.getClassName();
    final String serviceInstance = rpcServiceDiscovery.getServiceInstance(className);

    if (StringUtils.isBlank(serviceInstance)) {
      throw new RuntimeException(className + " has no available service instance.");
    }

    String[] split = serviceInstance.split(":");
    final Channel channel = ChannelManager.get(new InetSocketAddress(split[0], Integer.parseInt(split[1])));
  }
}
