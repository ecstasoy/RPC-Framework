package org.example.rpc.core.transport.server;

import io.netty.handler.timeout.IdleStateHandler;
import org.example.rpc.core.common.handler.ServerIdleStateHandler;
import org.example.rpc.core.process.RpcRequestProcessor;
import org.example.rpc.core.protocol.codec.MessageEncoder;
import org.example.rpc.core.protocol.codec.MessageDecoder;
import org.example.rpc.core.protocol.serialize.SerializerFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NettyServer {

  private final RpcRequestProcessor requestProcessor;
  private final NettyServerProperties properties;
  private final SerializerFactory serializerFactory;
  private static final int READER_IDLE_TIME = 60;

  public NettyServer(RpcRequestProcessor requestProcessor, 
                    NettyServerProperties properties,
                    SerializerFactory serializerFactory) {
    this.requestProcessor = requestProcessor;
    this.properties = properties;
    this.serializerFactory = serializerFactory;
  }
  
  public void start() {
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(
        Runtime.getRuntime().availableProcessors() * 2);

    try {
      ServerBootstrap bootstrap = new ServerBootstrap();
      bootstrap.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
              ChannelPipeline pipeline = ch.pipeline();
              pipeline
                  .addLast(new IdleStateHandler(READER_IDLE_TIME, 0, 0))
                  .addLast(new ServerIdleStateHandler())
                  .addLast(new MessageEncoder(serializerFactory))
                  .addLast(new MessageDecoder(serializerFactory))
                  .addLast(serviceHandlerGroup, new RpcServerSimpleChannelInboundHandlerImpl(requestProcessor));
            }
          });
      
      ChannelFuture future = bootstrap.bind(properties.getServerPort()).sync();
      log.info("NettyServer started on port: {}", properties.getServerPort());
      future.channel().closeFuture().sync();
    } catch (Exception e) {
      log.error("NettyServer start error", e);
    } finally {
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
      serviceHandlerGroup.shutdownGracefully();
    }
  }
}
