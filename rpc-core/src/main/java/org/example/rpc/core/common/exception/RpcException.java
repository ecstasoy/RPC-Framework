package org.example.rpc.core.common.exception;

public class RpcException extends RuntimeException {
  private final String errorCode;
  private final int httpStatus;

  public RpcException(String errorCode, String message, int httpStatus) {
    super(message);
    this.errorCode = errorCode;
    this.httpStatus = httpStatus;
  }

  public RpcException(String errorCode, String message, int httpStatus, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
    this.httpStatus = httpStatus;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public int getHttpStatus() {
    return httpStatus;
  }
}
