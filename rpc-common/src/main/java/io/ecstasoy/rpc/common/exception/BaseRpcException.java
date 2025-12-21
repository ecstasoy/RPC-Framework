package io.ecstasoy.rpc.common.exception;

import lombok.Data;

/**
 * Base RPC exception.
 *
 * <p>It is a base class for all RPC exceptions, which contains error code and HTTP status.</p>
 *
 * <p>It is an abstract class, so it cannot be instantiated.</p>
 *
 * <p>It is a subclass of {@link RuntimeException}.</p>
 *
 * <p>User should provide error code, message and HTTP status
 * when creating an instance of this class.</p>
 *
 * @author Kunhua Huang
 * @see RuntimeException
 */
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
