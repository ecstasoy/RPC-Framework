package org.example.rpc.api;

import org.example.rpc.core.annotations.RpcMethod;
import org.example.rpc.core.annotations.Param;
import java.util.concurrent.CompletableFuture;

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
  @RpcMethod
  CompletableFuture<User> selectById(@Param("id") String id);
}
