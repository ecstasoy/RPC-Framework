package org.example.rpc.api.exception;

import org.example.rpc.core.common.exception.BusinessException;

public class UserUpdateException extends BusinessException {
  private static final String ERROR_CODE = "USER_UPDATE_ERROR";

  public UserUpdateException(String message) {
    super(ERROR_CODE, message, 500); // 500 Internal Server Error
  }
}
