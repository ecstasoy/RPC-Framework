package org.example.rpc.client;

import lombok.extern.slf4j.Slf4j;
import org.example.rpc.api.User;
import org.example.rpc.api.UserService;
import org.example.rpc.core.common.annotations.Reference;
import org.example.rpc.core.common.exception.BaseRpcException;
import org.example.rpc.core.common.exception.RpcException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * BFF service.
 */
@Service
@Slf4j
public class BffService {

  @Reference
  private UserService userService;

  /**
   * Fetch user info by ID.
   *
   * @param id user ID
   * @return user info
   */
  public CompletableFuture<User> getUserInfo(String id) {
    log.info("Fetching user info for ID: {}", id);
    return userService.selectById(id)
        .thenApply(user -> {
          log.info("Fetched user info: {}", user);
          return user;
        })
        .exceptionally(this::handleException);
  }

  /**
   * Create a new user.
   *
   * @param user user info
   * @return created user info
   */
  public CompletableFuture<User> createUser(User user) {
    log.debug("Creating user: {}", user);
    return userService.createUser(user)
        .thenApply(createdUser -> {
          log.debug("Created user: {}", createdUser);
          return createdUser;
        })
        .exceptionally(this::handleException);
  }

  /**
   * Update user info.
   *
   * @param id   user ID
   * @param user user info
   * @return updated user info
   */
  public CompletableFuture<User> updateUser(String id, User user) {
    log.debug("Updating user info for ID: {}", id);
    return userService.updateUser(id, user)
        .thenApply(updatedUser -> {
          log.debug("Updated user: {}", updatedUser);
          return updatedUser;
        })
        .exceptionally(this::handleException);
  }

  /**
   * Delete user by ID.
   *
   * @param id user ID
   * @return void
   */
  public CompletableFuture<Void> deleteUser(String id) {
    log.debug("Deleting user with ID: {}", id);
    return userService.deleteUser(id)
        .thenRun(() -> log.debug("Deleted user with ID: {}", id))
        .exceptionally(this::handleException);
  }

  /**
   * Create multiple users.
   *
   * @param users user list
   * @return created users
   */
  public CompletableFuture<List<User>> createUsers(List<User> users) {
    log.debug("Creating users: {}", users);
    return userService.createUsers(users)
        .thenApply(createdUsers -> {
          log.debug("Created users: {}", createdUsers);
          return createdUsers;
        })
        .exceptionally(this::handleException);
  }

  /**
   * Fetch all users.
   *
   * @return all users
   */
  public CompletableFuture<List<User>> getAllUsers() {
    log.debug("Fetching all users");
    return userService.selectAll()
        .thenApply(users -> {
          log.debug("Fetched all users: {}", users);
          return users;
        })
        .exceptionally(this::handleException);
  }

  /**
   * Handle exception in the service.
   *
   * @param e exception
   * @param <T> return type
   * @return exception
   */
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