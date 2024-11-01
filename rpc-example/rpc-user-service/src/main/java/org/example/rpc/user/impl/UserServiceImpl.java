package org.example.rpc.user.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.example.rpc.api.exception.InvalidUserInputException;
import org.example.rpc.api.exception.user.DuplicateUserException;
import org.example.rpc.api.exception.user.UserDeletionException;
import org.example.rpc.api.exception.user.UserNotFoundException;
import org.example.rpc.api.exception.user.UserUpdateException;
import org.example.rpc.api.pojo.User;
import org.example.rpc.api.service.UserService;
import org.example.rpc.common.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
      if (userMap.containsKey(user.getId())) {
        throw new DuplicateUserException("User with ID " + user.getId() + " already exists");
      }
      if (user.getUsername() == null || user.getUsername().isEmpty()) {
        throw new InvalidUserInputException("Username cannot be null or empty");
      }
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
        throw new UserUpdateException("User not found, ID: " + id);
      }
      if (user.getUsername() == null || user.getUsername().isEmpty()) {
        throw new InvalidUserInputException("Username cannot be null or empty");
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
        throw new UserDeletionException("User not found, ID: " + id);
      }
      log.info("Deleted user with id: {}", id);
    });
  }

  @POST("/batch")
  @Override
  public CompletableFuture<List<User>> createUsers(@Body List<User> users) {
    return CompletableFuture.supplyAsync(() -> {
      // 首先检查是否有重复的指定id
      Set<String> specifiedIds = users.stream()
          .filter(u -> u.getId() != null)
          .map(User::getId)
          .collect(Collectors.toSet());

      if (specifiedIds.size() < users.stream()
          .filter(u -> u.getId() != null).count()) {
        throw new DuplicateUserException("Duplicate IDs are not allowed");
      }

      // 检查是否与现有用户id冲突
      for (String id : specifiedIds) {
        if (userMap.containsKey(id)) {
          throw new DuplicateUserException("User with id " + id + " already exists");
        }
      }

      List<User> createdUsers = new ArrayList<>();
      for (User user : users) {
        String id = user.getId() == null ? RandomStringUtils.randomAlphanumeric(6) : user.getId();
        user.setId(id);
        userMap.put(id, user);
        createdUsers.add(user);
        log.info("Created user: {}", user);
      }
      return createdUsers;
    });
  }

  @GET("/all")
  @Override
  public CompletableFuture<List<User>> selectAll() {
    return CompletableFuture.supplyAsync(() -> {
      log.info("Select all users");
      return new ArrayList<>(userMap.values());
    });
  }

  @GET("/{id}/exists")
  @Override
  public CompletableFuture<Void> validateUser(@Path("id") String id, @Query("username") String username) {
    return CompletableFuture.supplyAsync(() -> {
      log.info("Check if user exists with id: {}", id);
      if (!userMap.containsKey(id)) {
        throw new UserNotFoundException("User not found, ID: " + id);
      } else if (!userMap.get(id).getUsername().equals(username)) {
        throw new InvalidUserInputException("username does not match");
      }
      return null;
    });
  }
}
