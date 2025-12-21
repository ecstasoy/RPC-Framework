package io.ecstasoy.rpc.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Packet type for packet communication.
 *
 * <p>There are three types of packets:
 * <ul>
 *   <li>HEART_BEAT: heart beat</li>
 *   <li>RPC_REQUEST: RPC request</li>
 *   <li>RPC_RESPONSE: RPC response</li>
 * </ul>
 *
 * @author Kunhua Huang
 */
@AllArgsConstructor
@Getter
public enum PacketType {

  HEART_BEAT((byte) 0),

  RPC_REQUEST((byte) 1),

  RPC_RESPONSE((byte) 2);

  private final byte type;
}
