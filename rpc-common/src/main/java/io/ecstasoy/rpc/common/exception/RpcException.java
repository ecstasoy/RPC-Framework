package io.ecstasoy.rpc.common.exception;

/**
 * RPC Exception.
 *
 * <p>RPC Exception is a kind of exception that is thrown when an error occurs during the RPC process.
 * It is a subclass of BaseRpcException.
 * It indicates that the error is caused by the RPC process on the server side.
 * It is a checked exception, which means that the caller must handle it.
 *
 * @see BaseRpcException
 * @author Kunhua Huang
 */
public class RpcException extends BaseRpcException {

  /**
   * Constructor.
   *
   * @param errorCode error code
   * @param message error message
   */
  public RpcException(String errorCode, String message, int httpStatus) {
    super(errorCode, message, httpStatus);
  }
}
