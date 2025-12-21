package io.ecstasoy.rpc.protocol.model;

import lombok.*;
import io.ecstasoy.rpc.common.enums.PacketType;
import io.ecstasoy.rpc.protocol.model.packet.Packet;

import java.io.Serializable;
import java.util.Map;

/**
 * RPC request.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
public class RpcRequest extends Packet implements Serializable {

  /**
   * Serial version UID.
   */
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
  private Map<String, Object> parameters;

  /**
   * HTTP method.
   */
  private String httpMethod;

  /**
   * Request path.
   */
  private String path;

  /**
   * Query parameters.
   */
  private Map<String, String> queryParams;

  @Override
  public PacketType getPacketType() {
    return PacketType.RPC_REQUEST;
  }
}
