package org.example.rpc.api.exception.blog;

import org.example.rpc.core.common.exception.BusinessException;

public class DuplicateBlogException extends BusinessException {
  private static final String ERROR_CODE = "DUPLICATE_BLOG";

  public DuplicateBlogException(String message) {
    super(ERROR_CODE, message, 409); // 409 Conflict
  }
}

