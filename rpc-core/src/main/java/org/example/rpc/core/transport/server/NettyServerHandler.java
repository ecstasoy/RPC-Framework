package org.example.rpc.core.transport.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.example.rpc.core.model.RpcRequest;
import org.example.rpc.core.model.RpcResponse;
import org.example.rpc.core.process.RpcRequestProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private final RpcRequestProcessor requestProcessor;

    public NettyServerHandler(RpcRequestProcessor requestProcessor) {
        this.requestProcessor = requestProcessor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) {
        log.info("Server received request: {}", msg);
        requestProcessor.processRequest(msg).thenAccept(response -> {
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
