//package org.example.rpc.handler;
//
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelInboundHandlerAdapter;
//import io.netty.handler.timeout.IdleState;
//import io.netty.handler.timeout.IdleStateEvent;
//import lombok.extern.slf4j.Slf4j;
//import packet.model.protocol.rpc.io.ecstasoy.HeartBeatPacket;
//
//@Slf4j
//public class ServerIdleStateHandler extends ChannelInboundHandlerAdapter {
//  private static final int MAX_RETRY_COUNT = 3;
//  private int retryCount = 0;
//
//  @Override
//  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
//    if (evt instanceof IdleStateEvent) {
//      IdleStateEvent event = (IdleStateEvent) evt;
//      if (event.state() == IdleState.READER_IDLE) {
//        if (retryCount < MAX_RETRY_COUNT) {
//          log.info("No heartbeat from client, sending ping request. Retry count: {}", retryCount + 1);
//          HeartBeatPacket heartBeatPacket = new HeartBeatPacket();
//          heartBeatPacket.ping();
//          ctx.writeAndFlush(heartBeatPacket);
//          retryCount++;
//        } else {
//          log.warn("Client failed to respond after {} retries, closing connection", MAX_RETRY_COUNT);
//          ctx.close();
//        }
//      }
//    } else {
//      ctx.fireUserEventTriggered(evt);
//    }
//  }
//
//  @Override
//  public void channelRead(ChannelHandlerContext ctx, Object msg) {
//    if (msg instanceof HeartBeatPacket) {
//      HeartBeatPacket heartBeatPacket = (HeartBeatPacket) msg;
//      if (heartBeatPacket.isPing()) {
//        log.debug("Received ping from client, sending pong");
//        HeartBeatPacket response = new HeartBeatPacket();
//        response.pong();
//        ctx.writeAndFlush(response);
//        retryCount = 0;
//      }
//      return;
//    }
//    ctx.fireChannelRead(msg);
//  }
//}
