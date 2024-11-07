package org.example.rpc.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import org.example.rpc.protocol.model.packet.Packet;
import org.example.rpc.protocol.serialize.SerializerFactory;

/**
 * Message encoder.
 */
@Slf4j
public class MessageEncoder extends MessageToByteEncoder<Packet> {

  private final SerializerFactory serializerFactory;

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
    final byte type = msg.getSerializerType().getType();

    out.writeByte(msg.getMagicNum());
    out.writeByte(type);
    out.writeByte(msg.getPacketType().getType());

    final byte[] bytes = serializerFactory.getSerializer(type).serialize(msg);
    out.writeInt(bytes.length);
    out.writeBytes(bytes);

    log.debug("Message encoded, buffer size: {}", out.readableBytes());
  }
}
