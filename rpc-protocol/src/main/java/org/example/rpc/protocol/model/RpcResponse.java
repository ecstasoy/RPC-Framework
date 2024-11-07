package org.example.rpc.protocol.model;

import com.alibaba.fastjson.annotation.JSONField;
import org.example.rpc.common.enums.PacketType;
import org.example.rpc.common.exception.BaseRpcException;
import org.example.rpc.protocol.model.packet.Packet;
import org.example.rpc.protocol.serialize.impl.SimpleJsonSerializerImpl;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * RPC response.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class RpcResponse extends Packet implements Serializable {

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 6096668930426886407L;

  /**
   * Request ID.
   */
  private String sequence;

  /**
   * Throwable.
   */
  @JSONField(serializeUsing = SimpleJsonSerializerImpl.ThrowableSerializer.class,
      deserializeUsing = SimpleJsonSerializerImpl.ThrowableDeserializer.class)
  private Throwable throwable;

  /**
   * Result.
   */
  private Object result;

  private BaseRpcException rpcException;

  private String exceptionType;

  /**
   * Constructor.
   *
   * @param sequence request ID
   * @param throwable throwable
   */
  public RpcResponse(String sequence, Throwable throwable) {
    this.sequence = sequence;
    this.throwable = throwable;
    if (throwable instanceof BaseRpcException) {
      this.rpcException = (BaseRpcException) throwable;
      this.exceptionType = throwable.getClass().getName();
    }
  }

  /**
   * Constructor.
   *
   * @param sequence request ID
   * @param result result
   */
  public RpcResponse(String sequence, Object result) {
    this.sequence = sequence;
    this.result = result;
  }

  /**
   * No Args Constructor, only provided to avoid errors in serialization and deserialization.
   */
  public RpcResponse() {
  }

  @Override
  public PacketType getPacketType() {
    return PacketType.RPC_RESPONSE;
  }
}
