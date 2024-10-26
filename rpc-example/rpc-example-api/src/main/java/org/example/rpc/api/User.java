package org.example.rpc.api;

import lombok.Data;

import java.io.Serializable;

/**
 * User entity.
 */
@Data
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
