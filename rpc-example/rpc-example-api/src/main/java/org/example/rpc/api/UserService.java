package org.example.rpc.api;

import org.example.rpc.core.annotations.*;

import java.util.concurrent.CompletableFuture;

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
}