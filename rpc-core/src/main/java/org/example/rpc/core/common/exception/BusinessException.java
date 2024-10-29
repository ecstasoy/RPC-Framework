package org.example.rpc.core.common.exception;

/**
 * Exception caused by business logic.
 */
public class BusinessException extends BaseRpcException {

  /**
   * Constructor.
   * @param errorCode error code
   * @param message error message
   */
  public BusinessException(String errorCode, String message, int httpStatus) {
    super(errorCode, message, httpStatus);
  }
}
