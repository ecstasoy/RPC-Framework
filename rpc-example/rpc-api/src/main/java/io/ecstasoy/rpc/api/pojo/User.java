package io.ecstasoy.rpc.api.pojo;

import com.alibaba.fastjson.annotation.JSONType;
import lombok.Data;

import java.io.Serializable;

/**
 * User entity.
 */
@Data
@JSONType(typeName = "pojo.api.rpc.io.ecstasoy.User")
public class User implements Serializable {

  private String id;

  private String username;

  public User() {
  }

  public User(String id, String username) {
    this.id = id;
    this.username = username;
  }
}
