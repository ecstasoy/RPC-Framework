package org.example.rpc.client;

import org.example.rpc.api.User;
import org.example.rpc.api.UserService;
import org.example.rpc.core.annotations.Reference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
  public User getUserInfo() {
    log.info("Fetching user info...");
    String userId = "1";
    try {
      User user = userService.selectById(userId);
      log.info("User info: {}", user);
      return user;
    } catch (Exception e) {
      log.error("Error while fetching user info: {}", e.getMessage(), e);
      throw e; // If necessary, propagate the exception or handle it
    }
  }
}
