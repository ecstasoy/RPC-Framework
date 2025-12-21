package io.ecstasoy.rpc.api.exception;

import lombok.extern.slf4j.Slf4j;
import io.ecstasoy.rpc.api.ApiResponse;
import io.ecstasoy.rpc.common.exception.BaseRpcException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler.
 *
 * <p>Handle exceptions globally and return a unified response.
 * <p>Handle general exceptions and RPC exceptions.
 * <p>Log unexpected errors.
 *
 * @author Kunhua Huang
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handle general exceptions.
   *
   * @param ex the exception
   * @return the response entity
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<?>> handleGeneralException(Exception ex) {
    log.error("Unexpected error occurred:", ex);
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.toString(), ex.getMessage()));
  }

  /**
   * Handle RPC exceptions.
   *
   * @param ex the exception
   * @return the response entity
   */
  @ExceptionHandler(BaseRpcException.class)
  public ResponseEntity<ApiResponse<?>> handleBaseRpcException(BaseRpcException ex) {
    log.error("Unexpected error occurred in RPC:", ex);
    return ResponseEntity
        .status(ex.getHttpStatus())
        .body(ApiResponse.error(ex.getHttpStatus() + " " + ex.getErrorCode(), ex.getMessage()));
  }

}