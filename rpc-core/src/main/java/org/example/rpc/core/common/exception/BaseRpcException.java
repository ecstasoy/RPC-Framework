package org.example.rpc.core.common.exception;

import lombok.Data;

@Data
public abstract class BaseRpcException extends RuntimeException {

  private final String errorCode;
  private final int httpStatus;

  protected BaseRpcException(String errorCode, String message, int httpStatus) {
    super(message);
    this.errorCode = errorCode;
    this.httpStatus = httpStatus;
  }
}
