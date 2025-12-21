///*
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
//public class ClientIdleStateHandler extends ChannelInboundHandlerAdapter {
//  private static final int MAX_RETRY_COUNT = 3;
//  private int retryCount = 0;
//
//  @Override
//  public void channelRead(ChannelHandlerContext ctx, Object msg) {
//    if (msg instanceof HeartBeatPacket) {
//      HeartBeatPacket heartBeatPacket = (HeartBeatPacket) msg;
//      if (heartBeatPacket.isPong()) {
//        log.debug("Received pong from server");
//        retryCount = 0;
//        return;
//      }
//      ctx.fireChannelRead(msg);
//    }
//  }
//
//  @Override
//  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
//    if (evt instanceof IdleStateEvent) {
//      IdleStateEvent event = (IdleStateEvent) evt;
//      if (event.state() == IdleState.WRITER_IDLE) {
//        if (retryCount < MAX_RETRY_COUNT) {
//          log.info("Sending heartbeat to server, retry count: {}", retryCount + 1);
//          HeartBeatPacket heartBeatPacket = new HeartBeatPacket();
//          heartBeatPacket.ping();
//          ctx.writeAndFlush(heartBeatPacket);
//          retryCount++;
//        } else {
//          log.error("Server not responding after {} retries, closing connection", MAX_RETRY_COUNT);
//          ctx.close();
//        }
//      }
//    }
//  }
//}
//*/
