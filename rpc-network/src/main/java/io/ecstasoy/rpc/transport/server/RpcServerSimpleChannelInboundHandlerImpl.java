package io.ecstasoy.rpc.transport.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import io.ecstasoy.rpc.processor.RpcRequestProcessor;
import io.ecstasoy.rpc.protocol.model.RpcRequest;
import io.ecstasoy.rpc.protocol.model.RpcResponse;
import io.ecstasoy.rpc.protocol.model.packet.Packet;
import org.springframework.stereotype.Component;

/**
 * Rpc server simple channel inbound handler.
 */
@Slf4j
@Component
public class RpcServerSimpleChannelInboundHandlerImpl extends SimpleChannelInboundHandler<Packet> {

  private final RpcRequestProcessor requestProcessor;

  /**
   * Constructor.
   *
   * @param requestProcessor request processor
   */
  public RpcServerSimpleChannelInboundHandlerImpl(RpcRequestProcessor requestProcessor) {
    this.requestProcessor = requestProcessor;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    log.info("Channel active: {}", ctx.channel().remoteAddress());
    super.channelActive(ctx);
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    log.info("Channel read raw message: {}, type: {}", msg, msg.getClass().getName());
    super.channelRead(ctx, msg);
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Packet msg) {

    if (!(msg instanceof RpcRequest)) {
      log.warn("Received unexpected message type: {}", msg.getClass().getName());
      return;
    }

    final RpcRequest rpcRequest = (RpcRequest) msg;
    log.info("Server receive RPC request: [{}]", rpcRequest);

    requestProcessor.processRequest(rpcRequest)
        .thenAccept(rpcResponse -> {
          rpcResponse.setSerializerType(rpcRequest.getSerializerType());
          ctx.channel().writeAndFlush(rpcResponse)
              .addListener(future -> {
                if (!future.isSuccess()) {
                  log.error("Failed to send response", future.cause());
                }
              });
          log.info("Server processed RPC request: [{}], response: [{}]",
              rpcRequest.getSequence(), rpcResponse);
        })
        .exceptionally(throwable -> {
          log.error("Error processing RPC request", throwable);
          RpcResponse errorResponse = new RpcResponse(rpcRequest.getSequence(), null);
          errorResponse.setThrowable(throwable);
          ctx.channel().writeAndFlush(errorResponse)
              .addListener(future -> {
                if (!future.isSuccess()) {
                  log.error("Failed to send response", future.cause());
                }
              });
          return null;
        });
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    log.error("Server caught exception", cause);
    ctx.close();
  }
}
