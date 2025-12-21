package io.ecstasoy.rpc.transport.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import io.ecstasoy.rpc.processor.RpcRequestProcessor;
import io.ecstasoy.rpc.protocol.codec.MessageDecoder;
import io.ecstasoy.rpc.protocol.codec.MessageEncoder;
import io.ecstasoy.rpc.protocol.serialize.SerializerFactory;
import org.springframework.stereotype.Component;

/**
 * Netty server.
 */
@Slf4j
@Component
public class NettyServer {

  //  private static final int READER_IDLE_TIME = 180;
  private final RpcRequestProcessor requestProcessor;
  private final NettyServerProperties properties;
  private final SerializerFactory serializerFactory;

  /**
   * Constructor.
   *
   * @param requestProcessor RPC request processor
   * @param properties Netty server properties
   * @param serializerFactory serializer factory
   */
  public NettyServer(RpcRequestProcessor requestProcessor,
                     NettyServerProperties properties,
                     SerializerFactory serializerFactory) {
    this.requestProcessor = requestProcessor;
    this.properties = properties;
    this.serializerFactory = serializerFactory;
  }

  /**
   * Start the server.
   */
  public void start() {
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(
        Runtime.getRuntime().availableProcessors() * 2);

    try {
      ServerBootstrap bootstrap = new ServerBootstrap();
      bootstrap.group(bossGroup, workerGroup)
          .option(ChannelOption.SO_REUSEADDR, true)
          .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
          .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
          .childOption(ChannelOption.TCP_NODELAY, true)
          .childOption(ChannelOption.SO_KEEPALIVE, true)
          .childOption(ChannelOption.SO_REUSEADDR, true)
          .childOption(ChannelOption.SO_LINGER, 100)
          .channel(NioServerSocketChannel.class)
          .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
              ChannelPipeline pipeline = ch.pipeline();
              pipeline
                  /*.addLast(new IdleStateHandler(READER_IDLE_TIME, 0, 0))
                    .addLast(new ServerIdleStateHandler())*/
                  .addFirst("logger", new LoggingHandler(LogLevel.DEBUG))
                  .addLast(new MessageDecoder(serializerFactory))
                  .addLast(new MessageEncoder(serializerFactory))
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
