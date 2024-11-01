package org.example.rpc.protocol.model.packet;

import org.example.rpc.common.enums.PacketType;
import org.example.rpc.protocol.serialize.SerializerType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.io.Serializable;

/**
 * Packet.
 */
@Data
public abstract class Packet implements Serializable {

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 5058268849140034837L;

  /**
   * Magic number.
   */
  @Setter(AccessLevel.NONE)
  private byte magicNum = 66;

  /**
   * Get packet type.
   *
   * @return packet type
   */
  public abstract PacketType getPacketType();

  // 添加序列化类型字段
  @Setter(AccessLevel.NONE)
  private SerializerType serializerType = SerializerType.JSON;
}
