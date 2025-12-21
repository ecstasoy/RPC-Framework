package io.ecstasoy.rpc.protocol.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Error response.
 *
 * <p>Used to encapsulate error information in the response.
 * <p>It contains the error code and error message.
 *
 * @author Kunhua Huang
 */
@Data
@AllArgsConstructor
public class ErrorResponse {
  private String errorCode;
  private String message;
}
