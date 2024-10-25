package org.example.rpc.client;

import org.example.rpc.api.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.CompletableFuture;

/**
 * Bff controller.
 */
@RequestMapping("/test")
@RestController
public class BffController {

  @Autowired
  private BffService bffService;

  /**
   * Test user.
   *
   * @return user
   */
  @GetMapping
  public CompletableFuture<User> testUser() {
    return bffService.getUserInfo();
  }
}
