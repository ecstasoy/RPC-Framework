package org.example.rpc.core.protocol.codec;

import org.example.rpc.core.model.packet.Packet;
import org.example.rpc.core.protocol.serialize.SerializerFactory;
import org.example.rpc.core.protocol.serialize.SerializerType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

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
    final byte type = SerializerType.JSON.getType();

    out.writeByte(msg.getMagicNum());

    out.writeByte(type);

    out.writeByte(msg.getPacketType().getType());

    final byte[] bytes = serializerFactory.getSerializer(type).serialize(msg);
    out.writeInt(bytes.length);

    out.writeBytes(bytes);
  }
}
