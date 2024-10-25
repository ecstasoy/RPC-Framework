package org.example.rpc.core.client;

import org.example.rpc.core.codec.MessageEncoder;
import org.example.rpc.core.codec.MessageDecoder;
import org.example.rpc.core.network.ChannelManager;
import org.example.rpc.core.serialize.SerializerFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class NettyClient {

  private final Bootstrap bootstrap;
  private final EventLoopGroup eventLoopGroup;

  public NettyClient(SerializerFactory serializerFactory) {
    eventLoopGroup = new NioEventLoopGroup();
    bootstrap = new Bootstrap();
    bootstrap.group(eventLoopGroup)
        .channel(NioSocketChannel.class)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
        .handler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel ch) throws Exception {
            final ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
            pipeline.addLast(new MessageEncoder(serializerFactory));
            pipeline.addLast(new MessageDecoder(serializerFactory));
            pipeline.addLast(new RpcClientChannelInboundHandlerImpl());
          }
        });

    ChannelManager.setBootstrap(bootstrap);
  }
}
