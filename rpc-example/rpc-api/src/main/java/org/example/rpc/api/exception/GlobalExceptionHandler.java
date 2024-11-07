package org.example.rpc.api.exception;

import org.example.rpc.common.exception.BaseRpcException;
import org.example.rpc.api.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<?>> handleGeneralException(Exception ex) {
    log.error("Unexpected error occurred:", ex);
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.toString(), ex.getMessage()));
  }

  @ExceptionHandler(BaseRpcException.class)
  public ResponseEntity<ApiResponse<?>> handleBaseRpcException(BaseRpcException ex) {
    log.error("Unexpected error occurred in RPC:", ex);
    return ResponseEntity
        .status(ex.getHttpStatus())
        .body(ApiResponse.error(ex.getHttpStatus() + " " + ex.getErrorCode(), ex.getMessage()));
  }

}