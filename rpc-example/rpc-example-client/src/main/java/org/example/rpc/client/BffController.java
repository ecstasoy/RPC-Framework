package org.example.rpc.client;

import org.example.rpc.api.User;
import org.example.rpc.api.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * User Resource REST API Controller
 */
@RestController
@RequestMapping("/api/v1/users")
@Validated
public class BffController {

  private final BffService bffService;
  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  @Autowired
  public BffController(BffService bffService) {
    this.bffService = bffService;
  }

  // Single User Operations
  @GetMapping("/{id}")
  public CompletableFuture<String> getUser(@PathVariable String id) {
    return bffService.getUserInfo(id)
        .thenApply(user -> gson.toJson(ApiResponse.success(user)));
  }

  @PostMapping
  public CompletableFuture<String> createUser(@RequestBody @Validated User user) {
    return bffService.createUser(user)
        .thenApply(created -> gson.toJson(ApiResponse.success(created)));
  }

  @PutMapping("/{id}")
  public CompletableFuture<String> updateUser(@PathVariable String id, @RequestBody @Validated User user) {
    return bffService.updateUser(id, user)
        .thenApply(updated -> gson.toJson(ApiResponse.success(updated)));
  }

  @DeleteMapping("/{id}")
  public CompletableFuture<String> deleteUser(@PathVariable String id) {
    return bffService.deleteUser(id)
        .thenApply(v -> gson.toJson(ApiResponse.success(null)));
  }

  // Batch Operations
  @PostMapping("/batch")
  public CompletableFuture<String> createUsers(@RequestBody @Validated List<User> users) {
    return bffService.createUsers(users)
        .thenApply(createdUsers -> gson.toJson(ApiResponse.success(createdUsers)));
  }

  @GetMapping("/all")
  public CompletableFuture<String> getAllUsers() {
    return bffService.getAllUsers()
        .thenApply(users -> gson.toJson(ApiResponse.success(users)));
  }
}