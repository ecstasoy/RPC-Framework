package org.example.rpc.core.model;

import org.example.rpc.core.enums.PacketType;
import org.example.rpc.core.model.packet.Packet;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * RPC request.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class RpcRequest extends Packet implements Serializable {

  /**
   * Serial version UID.
   */
  @Serial
  private static final long serialVersionUID = 6096668930426886407L;

  /**
   * Request ID.
   */
  private String sequence;

  /**
   * Class name.
   */
  private String className;

  /**
   * Method name.
   */
  private String methodName;

  /**
   * Parameter types, used to identify the method.
   */
  private Class<?>[] parameterTypes;

  /**
   * Parameters.
   */
  private Object[] parameters;

  @Override
  public PacketType getPacketType() {
    return PacketType.RPC_REQUEST;
  }
}
