package io.ecstasoy.rpc.bff;

import lombok.extern.slf4j.Slf4j;
import io.ecstasoy.rpc.api.dto.request.CreateBlogDTO;
import io.ecstasoy.rpc.api.dto.request.CreateUserDTO;
import io.ecstasoy.rpc.api.dto.request.UpdateBlogDTO;
import io.ecstasoy.rpc.api.dto.request.UpdateUserDTO;
import io.ecstasoy.rpc.api.dto.response.BlogDTO;
import io.ecstasoy.rpc.api.dto.response.UserDTO;
import io.ecstasoy.rpc.api.service.BlogService;
import io.ecstasoy.rpc.api.service.UserService;
import io.ecstasoy.rpc.common.annotations.Reference;
import io.ecstasoy.rpc.common.exception.BaseRpcException;
import io.ecstasoy.rpc.common.exception.RpcException;
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

  @Reference
  private BlogService blogService;

  /**
   * Fetch user info by ID.
   *
   * @param id user ID
   * @return user info
   */
  public CompletableFuture<UserDTO> getUserInfo(String id) {
    log.info("Fetching user info for ID: {}", id);
    return userService.selectById(id)
        .thenApply(userDTO -> {
          log.info("Fetched user info: {}", userDTO);
          return userDTO;
        })
        .exceptionally(this::handleException);
  }

  /**
   * Create a new user.
   *
   * @param createUserDTO DTO for creating a user
   */
  public CompletableFuture<UserDTO> createUser(CreateUserDTO createUserDTO) {
    log.debug("Creating user: {}", createUserDTO);
    return userService.createUser(createUserDTO)
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
   * @param updateUserDTO DTO for updating a user
   * @return updated user info
   */
  public CompletableFuture<UserDTO> updateUser(String id, UpdateUserDTO updateUserDTO) {
    log.debug("Updating user info for ID: {}", id);
    return userService.updateUserId(id, updateUserDTO)
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
   * @param createUserDTOs user list
   * @return created users
   */
  public CompletableFuture<List<UserDTO>> createUsers(List<CreateUserDTO> createUserDTOs) {
    log.debug("Creating users: {}", createUserDTOs);
    return userService.createUsers(createUserDTOs)
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
  public CompletableFuture<List<UserDTO>> getAllUsers() {
    log.debug("Fetching all users");
    return userService.selectAll()
        .thenApply(users -> {
          log.debug("Fetched all users: {}", users);
          return users;
        })
        .exceptionally(this::handleException);
  }

  /**
   * Fetch blog info by ID.
   *
   * @param id blog ID
   * @return blog info
   */
  public CompletableFuture<BlogDTO> getBlogInfo(String id) {
    log.info("Fetching blog info for ID: {}", id);
    return blogService.selectById(id)
        .thenApply(blog -> {
          log.info("Fetched blog info: {}", blog);
          return blog;
        })
        .exceptionally(this::handleException);
  }

  /**
   * Create a new blog.
   *
   * @param createBlogDTO DTO for creating a blog
   * @return created blog info
   */
  public CompletableFuture<BlogDTO> createBlog(CreateBlogDTO createBlogDTO) {
    log.debug("Creating blog: {}", createBlogDTO);
    return blogService.createBlog(createBlogDTO)
        .thenApply(createdBlog -> {
          log.debug("Created blog: {}", createdBlog);
          return createdBlog;
        })
        .exceptionally(this::handleException);
  }

  /**
   * Update blog info.
   *
   * @param id   blog ID
   * @param updateBlogDTO DTO for updating a blog
   * @return updated blog info
   */
  public CompletableFuture<BlogDTO> updateBlog(String id, UpdateBlogDTO updateBlogDTO) {
    log.debug("Updating blog info for ID: {}", id);
    return blogService.updateBlog(id, updateBlogDTO)
        .thenApply(updatedBlog -> {
          log.debug("Updated blog: {}", updatedBlog);
          return updatedBlog;
        })
        .exceptionally(this::handleException);
  }

  /**
   * Delete blog by ID.
   *
   * @param id blog ID
   * @return void
   */
  public CompletableFuture<Void> deleteBlog(String id) {
    log.debug("Deleting blog with ID: {}", id);
    return blogService.deleteBlog(id)
        .thenRun(() -> log.debug("Deleted blog with ID: {}", id))
        .exceptionally(this::handleException);
  }

  /**
   * Create multiple blogs.
   *
   * @param createBlogDTOs blog list
   * @return created blogs
   */
  public CompletableFuture<List<BlogDTO>> createBlogs(List<CreateBlogDTO> createBlogDTOs) {
    log.debug("Creating blogs: {}", createBlogDTOs);
    return blogService.createBlogs(createBlogDTOs)
        .thenApply(createdBlogs -> {
          log.debug("Created blogs: {}", createdBlogs);
          return createdBlogs;
        })
        .exceptionally(this::handleException);
  }

  /**
   * Fetch all blogs.
   *
   * @return all blogs
   */
  public CompletableFuture<List<BlogDTO>> getAllBlogs() {
    log.debug("Fetching all blogs");
    return blogService.selectAll()
        .thenApply(blogs -> {
          log.debug("Fetched all blogs: {}", blogs);
          return blogs;
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

    Throwable cause = e;
    while (cause.getCause() != null) {
      cause = cause.getCause();
    }

    if (cause instanceof BaseRpcException) {
      throw (BaseRpcException) cause;
    }

    throw new RpcException("INTERNAL_SERVER_ERROR",
        cause.getMessage(),
        HttpStatus.INTERNAL_SERVER_ERROR.value());
  }
}