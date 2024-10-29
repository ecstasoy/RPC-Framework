package org.example.rpc.core.common.exception;

public class RpcException extends BaseRpcException {

  public RpcException(String errorCode, String message, int httpStatus) {
    super(errorCode, message, httpStatus);
  }
}
