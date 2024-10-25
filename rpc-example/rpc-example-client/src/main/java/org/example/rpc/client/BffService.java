package org.example.rpc.client;

import org.example.rpc.api.User;
import org.example.rpc.api.UserService;
import org.example.rpc.core.annotations.Reference;
import lombok.extern.slf4j.Slf4j;
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
  public CompletableFuture<User> getUserInfo() {
    log.info("Fetching user info...");
    String userId = "1";
    return userService.selectById(userId)
        .thenApply(user -> {
          log.info("User info: {}", user);
          return user;
        })
        .exceptionally(e -> {
          log.error("Error while fetching user info: {}", e.getMessage(), e);
          throw new CompletionException(e);
        });
  }
}
