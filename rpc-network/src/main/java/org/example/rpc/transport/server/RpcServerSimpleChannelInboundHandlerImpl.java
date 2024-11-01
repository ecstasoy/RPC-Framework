package org.example.rpc.transport.server;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.example.rpc.common.enums.PacketType;
import org.example.rpc.protocol.model.RpcRequest;
import org.example.rpc.protocol.model.RpcResponse;
import org.example.rpc.protocol.model.packet.HeartBeatPacket;
import org.example.rpc.protocol.model.packet.Packet;
import org.example.rpc.processor.RpcRequestProcessor;
import org.springframework.stereotype.Component;

/**
 * Rpc server simple channel inbound handler.
 */
@Slf4j
@Component
public class RpcServerSimpleChannelInboundHandlerImpl extends SimpleChannelInboundHandler<Packet> {

  private final RpcRequestProcessor requestProcessor;

  public RpcServerSimpleChannelInboundHandlerImpl(RpcRequestProcessor requestProcessor) {
    this.requestProcessor = requestProcessor;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Packet msg) throws Exception {
    final PacketType packetType = msg.getPacketType();

    log.info("Server receive message: [{}], packetType: [{}]", msg, packetType);

    if (packetType == PacketType.HEART_BEAT) {
      final HeartBeatPacket heartBeatPacket = new HeartBeatPacket();
      heartBeatPacket.pong();
      ctx.writeAndFlush(heartBeatPacket);
      log.debug("Heart beat response sent.");
    } else if (packetType == PacketType.RPC_REQUEST) {
      final RpcRequest rpcRequest = (RpcRequest) msg;
      requestProcessor.processRequest(rpcRequest)
          .thenAccept(rpcResponse -> {
            ctx.channel().writeAndFlush(rpcResponse).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            log.info("Server processed RPC request: [{}]-[{}], response: [{}]", 
                rpcRequest.getSequence(), packetType, rpcResponse);
          })
          .exceptionally(throwable -> {
            log.error("Error processing RPC request", throwable);
            RpcResponse errorResponse = new RpcResponse(rpcRequest.getSequence(), null);
            errorResponse.setThrowable(throwable);
            ctx.channel().writeAndFlush(errorResponse).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            return null;
          });
    }
  }
}
