package io.ecstasoy.rpc.api;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * API response.
 */
@Data
@AllArgsConstructor
public class ApiResponse<T> {
  private boolean success;
  private String message;
  private T data;
  private String errorCode;

  /**
   * Create a success response.
   *
   * @param data data
   * @return success response
   */
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, "Success", data, null);
  }

  /**
   * Create an error response.
   *
   * @param errorCode error code
   * @param message error message
   * @return error response
   */
  public static <T> ApiResponse<T> error(String errorCode, String message) {
    return new ApiResponse<>(false, message, null, errorCode);
  }
}