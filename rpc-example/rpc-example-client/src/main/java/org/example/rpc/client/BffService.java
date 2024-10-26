package org.example.rpc.client;

import lombok.extern.slf4j.Slf4j;
import org.example.rpc.api.User;
import org.example.rpc.api.UserService;
import org.example.rpc.core.annotations.Reference;
import org.example.rpc.core.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Bff service.
 */
@Service
@Slf4j
public class BffService {

  @Reference
  private UserService userService;

  /**
   * Get user info.
   *
   * @return user
   */
  public CompletableFuture<User> getUserInfo(String id) {
    log.info("Fetching user info...");
    String userId = id;
    return userService.selectById(userId)
        .thenApply(user -> {
          log.info("User info: {}", user);
          return user;
        })
        .exceptionally(e -> {
          log.error("Error when fetching user info: ", e);
          Throwable cause = e;
          while (cause.getCause() != null) {
            cause = cause.getCause();
          }
          if (cause instanceof BusinessException) {
            throw (BusinessException) cause;
          }
          throw new CompletionException(cause);
        });
  }

  public CompletableFuture<User> createUser(User user) {
    log.info("Creating user...");
    return userService.createUser(user)
        .thenApply(createdUser -> {
          log.info("Created user info: {}", createdUser);
          return createdUser;
        })
        .exceptionally(e -> {
          log.error("Error while creating user: {}", e.getMessage(), e);
          throw new CompletionException(e);
        });
  }

  public CompletableFuture<User> updateUser(String id, User user) {
    log.info("Updating user info...");
    return userService.updateUser(id, user)
        .thenApply(updatedUser -> {
          log.info("Updated user info: {}", updatedUser);
          return updatedUser;
        })
        .exceptionally(e -> {
          log.error("Error while updating user info: {}", e.getMessage(), e);
          throw new CompletionException(e);
        });
  }

  public CompletableFuture<Void> deleteUser(String id) {
    log.info("Deleting user...");
    return userService.deleteUser(id)
        .thenRun(() -> log.info("User deleted, ID: {}", id))
        .exceptionally(e -> {
          log.error("Error while deleting user: {}", e.getMessage(), e);
          throw new CompletionException(e);
        });
  }
}
