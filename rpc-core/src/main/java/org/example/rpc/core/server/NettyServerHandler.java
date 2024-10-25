package org.example.rpc.core.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.example.rpc.core.model.RpcRequest;
import org.example.rpc.core.model.RpcResponse;
import org.example.rpc.core.process.RpcRequestProcessor;

@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) {
        log.info("Server received request: {}", msg);
        RpcRequestProcessor.processRequest(msg).thenAccept(response -> {
            ctx.writeAndFlush(response);
            log.info("Server sent response: {}", response);
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Server caught exception", cause);
        ctx.close();
    }
}
