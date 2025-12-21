package io.ecstasoy.rpc.api.exception;

import io.ecstasoy.rpc.common.exception.BusinessException;

/**
 * Exception for invalid user input.
 */
public class InvalidUserInputException extends BusinessException {
  private static final String ERROR_CODE = "INVALID_USER_INPUT";

  /**
   * Constructor.
   *
   * @param message error message
   */
  public InvalidUserInputException(String message) {
    super(ERROR_CODE, message, 400); // 400 Bad Request
  }
}
