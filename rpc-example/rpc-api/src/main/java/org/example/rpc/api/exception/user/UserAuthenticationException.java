package org.example.rpc.api.exception.user;

import org.example.rpc.common.exception.BusinessException;

public class UserAuthenticationException extends BusinessException {
  private static final String ERROR_CODE = "USER_AUTHENTICATION_ERROR";

  public UserAuthenticationException(String message) {
    super(ERROR_CODE, message, 401); // 401 Unauthorized
  }
}
