package io.ecstasoy.rpc.api.exception.user;

import io.ecstasoy.rpc.common.exception.BusinessException;

/**
 * <p>Exception for user deletion.
 *
 * <p>Return 500 Internal Server Error.
 * <p>Code: USER_DELETION_ERROR
 */
public class UserDeletionException extends BusinessException {
  private static final String ERROR_CODE = "USER_DELETION_ERROR";

  public UserDeletionException(String message) {
    super(ERROR_CODE, message, 500); // 500 Internal Server Error
  }
}
