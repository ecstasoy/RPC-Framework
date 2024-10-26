package org.example.rpc.core.exception;

public class BusinessException extends RpcException {
  public BusinessException(String errorCode, String message, int httpStatus) {
    super(errorCode, message, httpStatus);
  }

  public BusinessException(String errorCode, String message, int httpStatus, Throwable cause) {
    super(errorCode, message, httpStatus, cause);
  }
}
