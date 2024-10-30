package org.example.rpc.core.common.handler;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.example.rpc.core.model.packet.HeartBeatPacket;

@Slf4j
public class ClientIdleStateHandler extends ChannelInboundHandlerAdapter {
  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
    if (evt instanceof IdleStateEvent) {
      IdleStateEvent event = (IdleStateEvent) evt;
      if (event.state() == IdleState.WRITER_IDLE) {
        log.info("Send heart beat to server.");
        HeartBeatPacket heartBeatPacket = new HeartBeatPacket();
        heartBeatPacket.ping();
        ctx.pipeline().writeAndFlush(heartBeatPacket);
      } else if (event.state() == IdleState.READER_IDLE) {
        log.warn("No response from server, close the connection.");
        ctx.close();
      }
    }
  }
}
