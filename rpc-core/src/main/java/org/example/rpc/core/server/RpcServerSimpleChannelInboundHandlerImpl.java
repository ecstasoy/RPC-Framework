package org.example.rpc.core.server;

import org.example.rpc.core.enums.PacketType;
import org.example.rpc.core.model.RpcRequest;
import org.example.rpc.core.model.RpcResponse;
import org.example.rpc.core.model.packet.Packet;
import org.example.rpc.core.model.packet.HeartBeatPacket;
import org.example.rpc.core.process.RpcRequestProcessor;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Rpc server simple channel inbound handler.
 */
@Slf4j
public class RpcServerSimpleChannelInboundHandlerImpl extends SimpleChannelInboundHandler<Object> {

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
    final PacketType packetType = ((Packet) msg).getPacketType();

    log.info("Server receive message: [{}], packetType: [{}]", msg, packetType);

    if (packetType == PacketType.HEART_BEAT) {
      final HeartBeatPacket heartBeatPacket = new HeartBeatPacket();
      heartBeatPacket.pong();
      ctx.writeAndFlush(heartBeatPacket).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    } else if (packetType == PacketType.RPC_REQUEST) {
      final RpcRequest rpcRequest = (RpcRequest) msg;
      final Object result = RpcRequestProcessor.process(rpcRequest);
      final RpcResponse rpcResponse = new RpcResponse(rpcRequest.getSequence(), result);

      ctx.channel().writeAndFlush(rpcResponse).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
      log.info("Server processed RPC request: [{}]-[{}], response: [{}]", rpcRequest.getSequence(), packetType, rpcResponse);
    }
  }
}
