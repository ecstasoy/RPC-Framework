package io.ecstasoy.rpc.api.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class UpdateBlogDTO {

  @NotBlank(message = "Title cannot be empty.")
  @Size(min = 1, max = 50, message = "Title length must be between 1-50.")
  private String title;

  @NotBlank(message = "Content cannot be empty.")
  private String content;

  @NotBlank(message = "Author ID cannot be empty.")
  private String authorId;
}
