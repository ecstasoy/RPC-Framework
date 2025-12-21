package io.ecstasoy.rpc.api.exception.user;

import io.ecstasoy.rpc.common.exception.BusinessException;

/**
 * User update exception.
 *
 * <p>It is an exception that will be thrown when updating user information fails.</p>
 *
 * @see BusinessException
 * @author Kunhua Huang
 */
public class UserUpdateException extends BusinessException {
  private static final String ERROR_CODE = "USER_UPDATE_ERROR";

  /**
   * Constructor.
   *
   * @param message error message
   */
  public UserUpdateException(String message) {
    super(ERROR_CODE, message, 500); // 500 Internal Server Error
  }
}
