package org.example.rpc.core.server;

import org.example.rpc.core.codec.MessageEncoder;
import org.example.rpc.core.codec.MessageDecoder;
import org.example.rpc.core.serialize.SerializerFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyServer {

  public NettyServer(int nettyPort, SerializerFactory serializerFactory) {
    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    DefaultEventExecutorGroup defaultEventExecutorGroup = new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors() * 2);

    ServerBootstrap bootstrap = new ServerBootstrap();
    bootstrap.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .childOption(ChannelOption.TCP_NODELAY, true)
        .childOption(ChannelOption.SO_KEEPALIVE, true)
        .option(ChannelOption.SO_BACKLOG, 128)
        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel ch) throws Exception {
            final ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new MessageEncoder(serializerFactory));
            pipeline.addLast(new MessageDecoder(serializerFactory));
            pipeline.addLast(new RpcServerSimpleChannelInboundHandlerImpl());

          }
        });

    final ChannelFuture future;
    try {
      future = bootstrap.bind(nettyPort).sync();
      log.info("Netty service successfully start: {}", nettyPort);
      future.channel().closeFuture().sync();
    } catch (InterruptedException e) {
      log.error("Netty failed to start: ", e);
    } finally {
      log.info("Netty service is closing...");
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
      defaultEventExecutorGroup.shutdownGracefully();
    }

  }
}
