package org.example.rpc.core.client;

import org.example.rpc.core.enums.PacketType;
import org.example.rpc.core.model.RpcResponse;
import org.example.rpc.core.model.packet.Packet;
import org.example.rpc.core.model.packet.HeartBeatPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * RPC client channel inbound handler implementation.
 */
@Slf4j
public class RpcClientChannelInboundHandlerImpl extends SimpleChannelInboundHandler<Packet> {

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Packet msg) throws Exception {
    log.info("Client receive message: [{}]", msg.toString());

    final PacketType packetType = msg.getPacketType();

    if (packetType == PacketType.HEART_BEAT) {
      HeartBeatPacket beatPacket = (HeartBeatPacket) msg;
      log.info("Heart beat packet received: [{}]", beatPacket.getFlag());
    } else {
      final RpcResponse response = (RpcResponse) msg;
      RequestFutureManager.removeAndComplete(response);
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    log.error("Exception caught: ", cause);
    ctx.close();
  }
}
