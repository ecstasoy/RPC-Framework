package org.example.rpc.protocol.codec;

import org.example.rpc.protocol.model.packet.Packet;
import org.example.rpc.protocol.serialize.Serializer;
import org.example.rpc.protocol.serialize.SerializerFactory;
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
    // Check if there are enough bytes to read message (magic number: 1, serializer type: 1, message type: 1, message length: 4)
    if (in.readableBytes() < 7) {
      return;
    }
    
    // Mark current read index
    in.markReaderIndex();
    
    // read magic number
    byte magicNum = in.readByte();
    if (magicNum != 66) {
      in.resetReaderIndex();
      throw new IllegalStateException("Invalid magic number: " + magicNum);
    }
    
    // read serializer type and message type
    byte serializerType = in.readByte();
    byte messageType = in.readByte();
    
    // read message length
    int length = in.readInt();
    if (length < 0) {
      throw new IllegalStateException("Invalid message length: " + length);
    }
    
    // If readable bytes are not enough, reset reader index and return
    if (in.readableBytes() < length) {
      in.resetReaderIndex();
      return;
    }

    byte[] bytes = new byte[length];
    in.readBytes(bytes);

    // 8. 反序列化
    Class<? extends Packet> packetClass = PacketClassManager.getPacketClass(messageType);
    if (packetClass == null) {
      throw new IllegalStateException("Unknown packet type: " + messageType);
    }

    Serializer serializer = serializerFactory.getSerializer(serializerType);
    Packet packet = serializer.deSerialize(bytes, packetClass);

    out.add(packet);
  }
}
