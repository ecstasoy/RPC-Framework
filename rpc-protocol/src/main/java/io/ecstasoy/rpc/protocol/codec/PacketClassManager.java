package io.ecstasoy.rpc.protocol.codec;

import io.ecstasoy.rpc.common.enums.PacketType;
import io.ecstasoy.rpc.protocol.model.RpcRequest;
import io.ecstasoy.rpc.protocol.model.RpcResponse;
import io.ecstasoy.rpc.protocol.model.packet.HeartBeatPacket;
import io.ecstasoy.rpc.protocol.model.packet.Packet;

import java.util.HashMap;
import java.util.Map;

/**
 * Packet class manager.
 */
public class PacketClassManager {

  /**
   * Packet type to packet class map.
   */
  private static final Map<Byte, Class<? extends Packet>> PACKET_CLASS_MAP = new HashMap<>();

  static {
    PACKET_CLASS_MAP.put(PacketType.HEART_BEAT.getType(), HeartBeatPacket.class);
    PACKET_CLASS_MAP.put(PacketType.RPC_REQUEST.getType(), RpcRequest.class);
    PACKET_CLASS_MAP.put(PacketType.RPC_RESPONSE.getType(), RpcResponse.class);
  }

  /**
   * Get packet class by type.
   *
   * @param type packet type
   * @return packet class
   */
  public static Class<? extends Packet> getPacketClass(byte type) {
    return PACKET_CLASS_MAP.get(type);
  }
}
