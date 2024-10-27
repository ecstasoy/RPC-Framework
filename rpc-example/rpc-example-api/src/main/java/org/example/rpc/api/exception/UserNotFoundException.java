package org.example.rpc.api.exception;

import org.example.rpc.core.common.exception.BusinessException;

public class UserNotFoundException extends BusinessException {
  private static final String ERROR_CODE = "USER_NOT_FOUND";

  public UserNotFoundException(String message) {
    super(ERROR_CODE, message, 404);
  }
}
