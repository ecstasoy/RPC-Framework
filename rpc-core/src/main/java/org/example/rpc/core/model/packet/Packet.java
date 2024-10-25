package org.example.rpc.core.model.packet;

import org.example.rpc.core.enums.PacketType;
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
}
