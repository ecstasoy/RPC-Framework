package io.ecstasoy.rpc.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.concurrent.FastThreadLocal;
import lombok.extern.slf4j.Slf4j;
import io.ecstasoy.rpc.protocol.model.packet.Packet;
import io.ecstasoy.rpc.protocol.serialize.Serializer;
import io.ecstasoy.rpc.protocol.serialize.SerializerFactory;
import io.ecstasoy.rpc.protocol.serialize.SerializerType;

import java.util.List;

/**
 * Message decoder.
 */
@Slf4j
public class MessageDecoder extends ByteToMessageDecoder {

  private final SerializerFactory serializerFactory;

  private static final FastThreadLocal<byte[]> BYTES_HOLDER = new FastThreadLocal<byte[]>() {
    @Override
    protected byte[] initialValue() {
      return new byte[1024 * 64];
    }
  };

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

    int length = in.readInt();
    if (length < 0 || length > 10 * 1024 * 1024) {
      throw new IllegalStateException("Invalid message length: " + length);
    }

    if (in.readableBytes() < length) {
      in.resetReaderIndex();
      return;
    }

    byte magicNum = in.readByte();
    if (magicNum != Packet.getMagicNum()) {
      in.resetReaderIndex();
      throw new IllegalStateException("Invalid magic number: " + magicNum);
    }

    byte serializerType = in.readByte();
    byte messageType = in.readByte();

    int bodyLength = length - 3;

    byte[] bytes = BYTES_HOLDER.get();
    if (length > bytes.length) {
      bytes = new byte[bodyLength];
    }

    in.readBytes(bytes, 0, bodyLength);

    Class<? extends Packet> packetClass = PacketClassManager.getPacketClass(messageType);
    if (packetClass == null) {
      throw new IllegalStateException("Unknown packet type: " + messageType);
    }

    try {
      Serializer serializer = serializerFactory.getSerializer(serializerType);
      byte[] messageBytes = new byte[bodyLength];
      System.arraycopy(bytes, 0, messageBytes, 0, bodyLength);
      Packet packet = serializer.deSerialize(messageBytes, packetClass);
      packet.setSerializerType(SerializerType.fromType(serializerType));
      out.add(packet);
    } catch (Exception e) {
      log.error("Failed to decode message", e);
      throw e;
    }
  }
}
