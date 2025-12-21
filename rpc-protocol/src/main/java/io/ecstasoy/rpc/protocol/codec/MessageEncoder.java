package io.ecstasoy.rpc.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.concurrent.FastThreadLocal;
import lombok.extern.slf4j.Slf4j;
import io.ecstasoy.rpc.protocol.model.packet.Packet;
import io.ecstasoy.rpc.protocol.serialize.SerializerFactory;

/**
 * Message encoder.
 */
@Slf4j
public class MessageEncoder extends MessageToByteEncoder<Packet> {

  private final SerializerFactory serializerFactory;

  private static final FastThreadLocal<ByteBuf> HEADER_BUF = new FastThreadLocal<ByteBuf>() {
    @Override
    protected ByteBuf initialValue() {
      return Unpooled.buffer(4);
    }
  };

  /**
   * Constructor.
   *
   * @param serializerFactory serializer factory
   */
  public MessageEncoder(SerializerFactory serializerFactory) {
    this.serializerFactory = serializerFactory;
  }

  @Override
  protected void encode(ChannelHandlerContext ctx, Packet msg, ByteBuf out) throws Exception {
    log.debug("Encoding message with serializer type: {}", msg.getSerializerType());

    ByteBuf headerBuf = HEADER_BUF.get();
    headerBuf.clear();

    final byte type = msg.getSerializerType().getType();

    headerBuf.writeByte(msg.getMagicNum());
    headerBuf.writeByte(type);
    headerBuf.writeByte(msg.getPacketType().getType());

    final byte[] bytes = serializerFactory.getSerializer(type).serialize(msg);
    out.writeInt(3 + bytes.length);
    out.writeBytes(headerBuf, 0, 3);
    out.writeBytes(bytes, 0, bytes.length);

    log.debug("Message encoded, buffer size: {}", out.readableBytes());
  }
}
