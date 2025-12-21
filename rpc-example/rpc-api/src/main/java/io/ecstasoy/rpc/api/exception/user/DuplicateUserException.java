package io.ecstasoy.rpc.api.exception.user;

import io.ecstasoy.rpc.common.exception.BusinessException;

public class DuplicateUserException extends BusinessException {
  private static final String ERROR_CODE = "DUPLICATE_USER";

  public DuplicateUserException(String message) {
    super(ERROR_CODE, message, 409); // 409 Conflict
  }
}

