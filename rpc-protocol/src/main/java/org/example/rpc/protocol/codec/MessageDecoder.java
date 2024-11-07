package org.example.rpc.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.example.rpc.protocol.model.packet.Packet;
import org.example.rpc.protocol.serialize.Serializer;
import org.example.rpc.protocol.serialize.SerializerFactory;
import org.example.rpc.protocol.serialize.SerializerType;

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
    // magic number: 1, serializer type: 1, message type: 1, message length: 4
    if (in.readableBytes() < 7) {
      return;
    }

    in.markReaderIndex();

    byte magicNum = in.readByte();
    if (magicNum != 66) {
      in.resetReaderIndex();
      throw new IllegalStateException("Invalid magic number: " + magicNum);
    }

    byte serializerType = in.readByte();
    byte messageType = in.readByte();

    int length = in.readInt();
    if (length < 0) {
      throw new IllegalStateException("Invalid message length: " + length);
    }

    if (in.readableBytes() < length) {
      in.resetReaderIndex();
      return;
    }

    byte[] bytes = new byte[length];
    in.readBytes(bytes);

    Class<? extends Packet> packetClass = PacketClassManager.getPacketClass(messageType);
    if (packetClass == null) {
      throw new IllegalStateException("Unknown packet type: " + messageType);
    }

    Serializer serializer = serializerFactory.getSerializer(serializerType);
    Packet packet = serializer.deSerialize(bytes, packetClass);
    packet.setSerializerType(SerializerType.fromType(serializerType));

    out.add(packet);
  }
}
