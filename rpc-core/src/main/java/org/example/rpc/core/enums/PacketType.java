package org.example.rpc.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PacketType {

  HEART_BEAT((byte) 0),

  RPC_REQUEST((byte) 1),

  RPC_RESPONSE((byte) 2);

  private byte type;
}
