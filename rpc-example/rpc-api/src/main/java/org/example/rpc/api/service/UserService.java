package org.example.rpc.api.service;

import org.example.rpc.api.pojo.User;
import org.example.rpc.core.common.annotations.*;

import java.util.concurrent.CompletableFuture;
import java.util.List;

@Api("/users")
public interface UserService {

  @GET("/{id}")
  CompletableFuture<User> selectById(@Path("id") String id);

  @POST
  CompletableFuture<User> createUser(@Body User user);

  @PUT("/{id}")
  CompletableFuture<User> updateUser(@Path("id") String id, @Body User user);

  @DELETE("/{id}")
  CompletableFuture<Void> deleteUser(@Path("id") String id);

  @POST("/batch")
  CompletableFuture<List<User>> createUsers(@Body List<User> users);

  @GET("/all")
  CompletableFuture<List<User>> selectAll();

  @GET("/{id}/exists")
  CompletableFuture<Void> validateUser(@Path("id") String id, @Query("username") String username);
}
