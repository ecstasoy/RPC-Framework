package io.ecstasoy.rpc.transport.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import io.ecstasoy.rpc.network.ChannelManager;
import io.ecstasoy.rpc.protocol.codec.MessageDecoder;
import io.ecstasoy.rpc.protocol.codec.MessageEncoder;
import io.ecstasoy.rpc.protocol.serialize.SerializerFactory;
import org.springframework.stereotype.Component;

/**
 * Netty client.
 */
@Slf4j
@Component
public class NettyClient {

  /*private static final int READER_IDLE_TIME = 60;
  private static final int WRITER_IDLE_TIME = 20;*/

  /**
   * Constructor.
   *
   * @param serializerFactory serializer factory
   */
  public NettyClient(SerializerFactory serializerFactory) {
    EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    Bootstrap bootstrap = new Bootstrap();
    bootstrap.group(eventLoopGroup)
        .channel(NioSocketChannel.class)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
        .handler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel ch) {
            final ChannelPipeline pipeline = ch.pipeline();
            /*pipeline.addLast(new IdleStateHandler(READER_IDLE_TIME, WRITER_IDLE_TIME, 0));
            pipeline.addLast(new ClientIdleStateHandler());*/
            pipeline.addLast(new MessageEncoder(serializerFactory));
            pipeline.addLast(new MessageDecoder(serializerFactory));
            pipeline.addLast(new RpcClientChannelInboundHandlerImpl());
          }
        });

    ChannelManager.setBootstrap(bootstrap);
  }
}
