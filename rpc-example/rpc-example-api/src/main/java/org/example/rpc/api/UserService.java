package org.example.rpc.api;

/**
 * Interface of user service.
 */
public interface UserService {

  /**
   * Select user by id.
   *
   * @param id id
   * @return user
   */
  User selectById(String id);
}
