package org.example.rpc.api.dto.response;

import lombok.Data;

@Data
public class UserDTO {
  private String id;
  private String username;
  private String email;
  private Long createTime;
  private Long updateTime;
}
