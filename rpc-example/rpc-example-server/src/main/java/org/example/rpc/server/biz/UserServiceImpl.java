package org.example.rpc.server.biz;

import org.example.rpc.api.User;
import org.example.rpc.api.UserService;
import org.example.rpc.api.exception.UserNotFoundException;
import org.example.rpc.core.annotations.*;
import org.apache.commons.lang3.RandomStringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Slf4j
@RpcService
public class UserServiceImpl implements UserService {

  private final Map<String, User> userMap = new ConcurrentHashMap<>();

  @GET("/{id}")
  @Override
  public CompletableFuture<User> selectById(@Path("id") String id) {
    return CompletableFuture.supplyAsync(() -> {
      User user = userMap.get(id);
      if (user == null) {
        log.warn("User not found with id: {}", id);
        throw new UserNotFoundException("User not found, ID: " + id);
      }
      log.info("Select user by id: {}", id);
      return user;
    });
  }

  @POST
  @Override
  public CompletableFuture<User> createUser(@Body User user) {
    return CompletableFuture.supplyAsync(() -> {
      String id = user.getId() == null ? RandomStringUtils.randomAlphanumeric(6) : user.getId();
      user.setId(id);
      userMap.put(id, user);
      log.info("Created user: {}", user);
      return user;
    });
  }

  @PUT("/{id}")
  @Override
  public CompletableFuture<User> updateUser(@Path("id") String id, @Body User user) {
    return CompletableFuture.supplyAsync(() -> {
      User existingUser = userMap.get(id);
      if (existingUser == null) {
        log.warn("User not found with id: {}", id);
        throw new RuntimeException("User not found");
      }
      existingUser.setUsername(user.getUsername());
      log.info("Updated user: {}", existingUser);
      return existingUser;
    });
  }

  @DELETE("/{id}")
  @Override
  public CompletableFuture<Void> deleteUser(@Path("id") String id) {
    return CompletableFuture.runAsync(() -> {
      User removedUser = userMap.remove(id);
      if (removedUser == null) {
        throw new RuntimeException("User not found");
      }
      log.info("Deleted user with id: {}", id);
    });
  }
}