package org.example.rpc.core.transport.client;

import org.example.rpc.core.common.handler.ClientIdleStateHandler;
import org.example.rpc.core.protocol.codec.MessageEncoder;
import org.example.rpc.core.protocol.codec.MessageDecoder;
import org.example.rpc.core.network.ChannelManager;
import org.example.rpc.core.protocol.serialize.SerializerFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class NettyClient {

  private final Bootstrap bootstrap;
  private final EventLoopGroup eventLoopGroup;
  private static final int READER_IDLE_TIME = 30; // 读超时 30s
  private static final int WRITER_IDLE_TIME = 10; // 写超时 10s

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
            pipeline.addLast(new IdleStateHandler(READER_IDLE_TIME, WRITER_IDLE_TIME, 0));
            pipeline.addLast(new ClientIdleStateHandler());
            pipeline.addLast(new MessageEncoder(serializerFactory));
            pipeline.addLast(new MessageDecoder(serializerFactory));
            pipeline.addLast(new RpcClientChannelInboundHandlerImpl());
          }
        });

    ChannelManager.setBootstrap(bootstrap);
  }
}
