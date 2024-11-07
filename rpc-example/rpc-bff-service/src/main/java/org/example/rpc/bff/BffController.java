package org.example.rpc.bff;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.rpc.api.ApiResponse;
import org.example.rpc.api.dto.request.CreateBlogDTO;
import org.example.rpc.api.dto.request.CreateUserDTO;
import org.example.rpc.api.dto.request.UpdateBlogDTO;
import org.example.rpc.api.dto.request.UpdateUserDTO;
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
   * @param createUserDTO user DTO
   * @return created user JSON
   */
  @PostMapping("users")
  public CompletableFuture<String> createUser(@RequestBody @Validated CreateUserDTO createUserDTO) {
    return bffService.createUser(createUserDTO)
        .thenApply(created -> gson.toJson(ApiResponse.success(created)));
  }

  /**
   * Update user by ID.
   *
   * @param id user ID
   * @param updateUserDTO user DTO
   * @return updated user JSON
   */
  @PutMapping("users/{id}")
  public CompletableFuture<String> updateUser(@PathVariable String id, @RequestBody @Validated UpdateUserDTO updateUserDTO) {
    return bffService.updateUser(id, updateUserDTO)
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
   * @param createUserDTOs user DTOs
   * @return created users JSON
   */
  @PostMapping("users/batch")
  public CompletableFuture<String> createUsers(@RequestBody @Validated List<CreateUserDTO> createUserDTOs) {
    return bffService.createUsers(createUserDTOs)
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

  /**
   * Create a new blog.
   *
   * @param createBlogDTO blog DTO
   * @return created blog JSON
   */
  @PostMapping("blogs")
  public CompletableFuture<String> createBlog(@RequestBody @Validated CreateBlogDTO createBlogDTO) {
    return bffService.createBlog(createBlogDTO)
        .thenApply(created -> gson.toJson(ApiResponse.success(created)));
  }

  /**
   * Uodate blog by ID.
   *
   * @param id blog ID
   */
  @PutMapping("blogs/{id}")
  public CompletableFuture<String> updateBlog(@PathVariable String id, @RequestBody @Validated UpdateBlogDTO updateBlogDTO) {
    return bffService.updateBlog(id, updateBlogDTO)
        .thenApply(updated -> gson.toJson(ApiResponse.success(updated)));
  }

  /**
   * Delete blog by ID.
   *
   * @param id blog ID
   * @return success JSON
   */
  @DeleteMapping("blogs/{id}")
  public CompletableFuture<String> deleteBlog(@PathVariable String id) {
    return bffService.deleteBlog(id)
        .thenApply(v -> gson.toJson(ApiResponse.success(null)));
  }

  /**
   * Create multiple blogs.
   *
   * @param createBlogDTOs blog DTOs
   * @return created blogs JSON
   */
  @PostMapping("blogs/batch")
  public CompletableFuture<String> createBlogs(@RequestBody @Validated List<CreateBlogDTO> createBlogDTOs) {
    return bffService.createBlogs(createBlogDTOs)
        .thenApply(createdBlogs -> gson.toJson(ApiResponse.success(createdBlogs)));
  }

  /**
   * Get all blogs.
   *
   * @return all blogs JSON
   */
  @GetMapping("blogs/all")
  public CompletableFuture<String> getAllBlogs() {
    return bffService.getAllBlogs()
        .thenApply(blogs -> gson.toJson(ApiResponse.success(blogs)));
  }
}