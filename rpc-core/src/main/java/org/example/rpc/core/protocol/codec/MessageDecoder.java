package org.example.rpc.core.protocol.codec;

import org.example.rpc.core.model.packet.Packet;
import org.example.rpc.core.protocol.serialize.Serializer;
import org.example.rpc.core.protocol.serialize.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Message decoder.
 */
@Slf4j
public class MessageDecoder extends ByteToMessageDecoder {

  private final SerializerFactory serializerFactory;

  /**
   * Constructor.
   *
   * @param serializerFactory serializer factory
   */
  public MessageDecoder(SerializerFactory serializerFactory) {
    this.serializerFactory = serializerFactory;
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

    in.skipBytes(1);

    final byte serializerType = in.readByte();

    final byte messageType = in.readByte();
    final Class<? extends Packet> packetClass = PacketClassManager.getPacketClass(messageType);
    if (packetClass == null) {
      throw new UnsupportedOperationException("Unsupported packet type: " + messageType);
    }

    final int length = in.readInt();

    final Serializer serializer = serializerFactory.getSerializer(serializerType);
    if (serializer == null) {
      throw new UnsupportedOperationException("Unsupported serializer type: " + serializerType);
    }

    final byte[] bytes = new byte[length];
    in.readBytes(bytes);

    final Packet packet = serializer.deSerialize(bytes, packetClass);
    out.add(packet);
  }
}
