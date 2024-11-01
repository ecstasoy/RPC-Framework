package org.example.rpc.api.exception.blog;

import org.example.rpc.common.exception.BusinessException;

public class BlogNotFoundException extends BusinessException {
  private static final String ERROR_CODE = "BLOG_NOT_FOUND";

  public BlogNotFoundException(String message) {
    super(ERROR_CODE, message, 404);
  }
}
