package org.example.rpc.api.exception;

import org.example.rpc.common.exception.BusinessException;

public class InvalidUserInputException extends BusinessException {
  private static final String ERROR_CODE = "INVALID_USER_INPUT";

  public InvalidUserInputException(String message) {
    super(ERROR_CODE, message, 400); // 400 Bad Request
  }
}
