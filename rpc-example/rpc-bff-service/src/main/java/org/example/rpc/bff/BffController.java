package org.example.rpc.bff;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.example.rpc.api.ApiResponse;
import org.example.rpc.api.pojo.Blog;
import org.example.rpc.api.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * User Resource REST API Controller.
 */
@RestController
@RequestMapping("/api/v1")
@Validated
public class BffController {

  private final BffService bffService;
  private final Gson gson = new GsonBuilder()
      .setPrettyPrinting()
      .disableHtmlEscaping()
      .create();

  /**
   * Constructor.
   *
   * @param bffService BFF service
   */
  @Autowired
  public BffController(BffService bffService) {
    this.bffService = bffService;
  }

  /**
   * Get user by ID.
   *
   * @param id user ID
   * @return user JSON
   */
  @GetMapping("users/{id}")
  public CompletableFuture<String> getUser(@PathVariable String id) {
    return bffService.getUserInfo(id)
        .thenApply(user -> gson.toJson(ApiResponse.success(user)));
  }

  /**
   * Create a new user.
   *
   * @param user user
   * @return created user JSON
   */
  @PostMapping("users")
  public CompletableFuture<String> createUser(@RequestBody @Validated User user) {
    return bffService.createUser(user)
        .thenApply(created -> gson.toJson(ApiResponse.success(created)));
  }

  /**
   * Update user by ID.
   *
   * @param id user ID
   * @param user user
   * @return updated user JSON
   */
  @PutMapping("users/{id}")
  public CompletableFuture<String> updateUser(@PathVariable String id, @RequestBody @Validated User user) {
    return bffService.updateUser(id, user)
        .thenApply(updated -> gson.toJson(ApiResponse.success(updated)));
  }

  /**
   * Delete user by ID.
   *
   * @param id user ID
   * @return success JSON
   */
  @DeleteMapping("users/{id}")
  public CompletableFuture<String> deleteUser(@PathVariable String id) {
    return bffService.deleteUser(id)
        .thenApply(v -> gson.toJson(ApiResponse.success(null)));
  }

  /**
   * Create multiple users.
   *
   * @param users users
   * @return created users JSON
   */
  @PostMapping("users/batch")
  public CompletableFuture<String> createUsers(@RequestBody @Validated List<User> users) {
    return bffService.createUsers(users)
        .thenApply(createdUsers -> gson.toJson(ApiResponse.success(createdUsers)));
  }

  /**
   * Get all users.
   *
   * @return all users JSON
   */
  @GetMapping("users/all")
  public CompletableFuture<String> getAllUsers() {
    return bffService.getAllUsers()
        .thenApply(users -> gson.toJson(ApiResponse.success(users)));
  }

  @PostMapping("blogs")
  public CompletableFuture<String> createBlog(@RequestBody @Validated Blog blog) {
    return bffService.createBlog(blog)
        .thenApply(created -> gson.toJson(ApiResponse.success(created)));
  }

  @PutMapping("blogs/{id}")
  public CompletableFuture<String> updateBlog(@PathVariable String id, @RequestBody @Validated Blog blog) {
    return bffService.updateBlog(id, blog)
        .thenApply(updated -> gson.toJson(ApiResponse.success(updated)));
  }

  @DeleteMapping("blogs/{id}")
  public CompletableFuture<String> deleteBlog(@PathVariable String id) {
    return bffService.deleteBlog(id)
        .thenApply(v -> gson.toJson(ApiResponse.success(null)));
  }

  @PostMapping("blogs/batch")
  public CompletableFuture<String> createBlogs(@RequestBody @Validated List<Blog> blogs) {
    return bffService.createBlogs(blogs)
        .thenApply(createdBlogs -> gson.toJson(ApiResponse.success(createdBlogs)));
  }

  @GetMapping("blogs/all")
  public CompletableFuture<String> getAllBlogs() {
    return bffService.getAllBlogs()
        .thenApply(blogs -> gson.toJson(ApiResponse.success(blogs)));
  }
}