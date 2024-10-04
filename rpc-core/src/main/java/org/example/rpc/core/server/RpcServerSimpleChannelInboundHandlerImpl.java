package org.example.rpc.core.server;

import org.example.rpc.core.enums.PacketType;
import org.example.rpc.core.model.RpcRequest;
import org.example.rpc.core.model.RpcResponse;
import org.example.rpc.core.model.packet.Packet;
import org.example.rpc.core.model.packet.HeartBeatPacket;
import org.example.rpc.core.model.process.RpcRequestProcessor;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcServerSimpleChannelInboundHandlerImpl extends SimpleChannelInboundHandler<Object> {

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
    log.info("Server receive message: [{}]", msg);
  }
}
