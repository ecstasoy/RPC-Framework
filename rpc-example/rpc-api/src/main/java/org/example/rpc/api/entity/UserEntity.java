package org.example.rpc.api.entity;

import lombok.Data;

@Data
public class UserEntity {
  private String id;
  private String username;
  private String password;
  private String email;
  private Long createTime;
  private Long updateTime;
}
