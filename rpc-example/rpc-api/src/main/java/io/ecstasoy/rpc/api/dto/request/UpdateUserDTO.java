package io.ecstasoy.rpc.api.dto.request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class UpdateUserDTO {

  @NotBlank(message = "ID cannot be empty")
  @Size(min = 4, max = 20, message = "Username length must be between 4-20")
  private String username;

  @NotBlank(message = "Password cannot be empty")
  @Size(min = 6, max = 20, message = "Password length must be between 6-20")
  private String password;

  @NotBlank(message = "Email cannot be empty")
  @Email(message = "Email format is wrong")
  private String email;
}