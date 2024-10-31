package org.example.rpc.common.exception;

/**
 * Exception caused by business logic.
 *
 * <p>It is a subclass of {@link BaseRpcException}.
 * <p>It is used to indicate that the exception is caused by business logic.
 * <p>User should handle this exception in the business layer.
 *
 * @see BaseRpcException
 * @see RpcException
 * @author Kunhua Huang
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
