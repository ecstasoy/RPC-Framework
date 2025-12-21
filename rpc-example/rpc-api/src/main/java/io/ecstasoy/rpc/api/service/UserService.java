package io.ecstasoy.rpc.api.service;

import io.ecstasoy.rpc.api.dto.request.CreateUserDTO;
import io.ecstasoy.rpc.api.dto.request.UpdateUserDTO;
import io.ecstasoy.rpc.api.dto.response.UserDTO;
import io.ecstasoy.rpc.common.annotations.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * User service.
 *
 * <p>Provide user-related services.
 *
 * @author Kunhua Huang
 */
@Api("/users")
public interface UserService {

  /**
   * Select user by ID.
   *
   * @param id user ID
   * @return user
   */
  @GET("/{id}")
  CompletableFuture<UserDTO> selectById(@Path("id") String id);

  /**
   * Create a new user.
   *
   * @param user user
   * @return created user
   */
  @POST
  CompletableFuture<UserDTO> createUser(@Body CreateUserDTO createUserDTO);

  /**
   * Update user.
   *
   * @param id   user ID
   * @param user user
   * @return updated user
   */
  @PUT("/{id}")
  CompletableFuture<UserDTO> updateUserId(@Path("id") String id, @Body UpdateUserDTO updateUserDTO);

  /**
   * Delete user by ID.
   *
   * @param id user ID
   */
  @DELETE("/{id}")
  CompletableFuture<Void> deleteUser(@Path("id") String id);

  /**
   * Batch create users.
   *
   * @param users users
   * @return created users
   */
  @POST("/batch")
  CompletableFuture<List<UserDTO>> createUsers(@Body List<CreateUserDTO> createUserDTOS);

  /**
   * Select all users.
   *
   * @return all users
   */
  @GET("/all")
  CompletableFuture<List<UserDTO>> selectAll();

  /**
   * Validate user.
   *
   * @param id       user ID
   * @param username username
   */
  @GET("/{id}/exists")
  CompletableFuture<Void> validateUser(@Path("id") String id, @Query("username") String username);
}
