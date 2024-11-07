package org.example.rpc.protocol.model.packet;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.rpc.common.enums.PacketType;

/**
 * Heartbeat packet.
 */
@Getter
public class HeartBeatPacket extends Packet {

  public static final byte PING = 1;
  public static final byte PONG = 2;

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

  public boolean isPing() {
    return this.flag == PING;
  }

  public boolean isPong() {
    return this.flag == PONG;
  }
}