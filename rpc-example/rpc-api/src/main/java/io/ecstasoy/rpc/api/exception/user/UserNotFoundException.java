package io.ecstasoy.rpc.api.exception.user;

import io.ecstasoy.rpc.common.exception.BusinessException;

/**
 * User not found exception.
 *
 * <p>Thrown when the user is not found.
 * <p>HTTP status code: 404
 *
 * @author Kunhua Huang
 */
public class UserNotFoundException extends BusinessException {
  private static final String ERROR_CODE = "USER_NOT_FOUND";

  public UserNotFoundException(String message) {
    super(ERROR_CODE, message, 404);
  }
}
