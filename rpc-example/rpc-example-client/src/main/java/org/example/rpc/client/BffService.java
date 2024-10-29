package org.example.rpc.client;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.rpc.api.User;
import org.example.rpc.api.UserService;
import org.example.rpc.core.common.annotations.Reference;
import org.example.rpc.core.common.exception.BaseRpcException;
import org.example.rpc.core.common.exception.BusinessException;
import org.example.rpc.core.common.exception.RpcException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Service
@Slf4j
public class BffService {

  @Reference
  private UserService userService;

  public CompletableFuture<User> getUserInfo(String id) {
    log.info("Fetching user info for ID: {}", id);
    return userService.selectById(id)
        .thenApply(user -> {
          log.info("Fetched user info: {}", user);
          return user;
        })
        .exceptionally(this::handleException);
  }

  public CompletableFuture<User> createUser(User user) {
    log.info("Creating user: {}", user);
    return userService.createUser(user)
        .thenApply(createdUser -> {
          log.info("Created user: {}", createdUser);
          return createdUser;
        })
        .exceptionally(this::handleException);
  }

  public CompletableFuture<User> updateUser(String id, User user) {
    log.info("Updating user info for ID: {}", id);
    return userService.updateUser(id, user)
        .thenApply(updatedUser -> {
          log.info("Updated user: {}", updatedUser);
          return updatedUser;
        })
        .exceptionally(this::handleException);
  }

  public CompletableFuture<Void> deleteUser(String id) {
    log.info("Deleting user with ID: {}", id);
    return userService.deleteUser(id)
        .thenRun(() -> log.info("Deleted user with ID: {}", id))
        .exceptionally(this::handleException);
  }

  public CompletableFuture<List<User>> createUsers(List<User> users) {
    log.info("Creating users: {}", users);
    return userService.createUsers(users)
        .thenApply(createdUsers -> {
          log.info("Created users: {}", createdUsers);
          return createdUsers;
        })
        .exceptionally(this::handleException);
  }

  public CompletableFuture<List<User>> getAllUsers() {
    log.info("Fetching all users");
    return userService.selectAll()
        .thenApply(users -> {
          log.info("Fetched all users: {}", users);
          return users;
        })
        .exceptionally(this::handleException);
  }

  private <T> T handleException(Throwable e) {
    log.error("Error occurred: {}", e.getMessage(), e);
    // find the root cause
    Throwable cause = e;
    while (cause.getCause() != null) {
      cause = cause.getCause();
    }

    if (cause instanceof BaseRpcException) {
      throw (BaseRpcException) cause;
    }

    throw new RpcException("INTERNAL_ERROR",
        e.getMessage(),
        HttpStatus.INTERNAL_SERVER_ERROR.value());
  }
}