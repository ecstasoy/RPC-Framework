package org.example.rpc.transport.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.example.rpc.common.enums.PacketType;
import org.example.rpc.core.protocol.proto.Rpc;
import org.example.rpc.protocol.model.RpcResponse;
import org.example.rpc.protocol.model.packet.Packet;

/**
 * RPC client channel inbound handler implementation.
 */
@Slf4j
public class RpcClientChannelInboundHandlerImpl extends SimpleChannelInboundHandler<Packet> {

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Packet msg) {
    final PacketType packetType = msg.getPacketType();
    log.info("Client receive message: [{}]", msg.toString());
    final RpcResponse response = (RpcResponse) msg;
    RequestFutureManager.removeAndComplete(response);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    log.error("Exception caught: ", cause);
    ctx.close();
  }
}
