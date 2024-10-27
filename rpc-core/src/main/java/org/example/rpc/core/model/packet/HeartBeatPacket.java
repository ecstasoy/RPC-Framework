package org.example.rpc.core.model.packet;

import org.example.rpc.core.common.enums.PacketType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Heartbeat packet.
 */
public class HeartBeatPacket extends Packet {

  public static final byte PING = 1;
  public static final byte PONG = 2;

  @Getter
  @Setter(AccessLevel.NONE)
  private byte flag;

  public void ping() {
    this.flag = PING;
  }

  public void pong() {
    this.flag = PONG;
  }


  @Override
  public PacketType getPacketType() {
    return PacketType.HEART_BEAT;
  }
}