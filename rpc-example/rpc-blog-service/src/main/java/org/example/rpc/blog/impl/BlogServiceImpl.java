package org.example.rpc.blog.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.example.rpc.api.exception.blog.BlogNotFoundException;
import org.example.rpc.api.exception.blog.DuplicateBlogException;
import org.example.rpc.api.exception.InvalidUserInputException;
import org.example.rpc.api.pojo.Blog;
import org.example.rpc.api.pojo.User;
import org.example.rpc.api.service.BlogService;
import org.example.rpc.api.service.UserService;
import org.example.rpc.common.annotations.*;
import org.example.rpc.common.exception.BaseRpcException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RpcService
public class BlogServiceImpl implements BlogService {

  private final Map<String, Blog> blogMap = new ConcurrentHashMap<>();

  @Reference
  private UserService userService;

  @GET("/{id}")
  @Override
  public CompletableFuture<Blog> selectById(@Path("id") String id) {
    return CompletableFuture.supplyAsync(() -> {
      log.debug("Selecting blog by id: {}", id);
      Blog blog = blogMap.get(id);
      if (blog == null) {
        log.warn("Blog not found with id: {}", id);
        throw new BlogNotFoundException("Blog not found, ID: " + id);
      }
      log.info("Select blog by id: {}", id);
      return blog;
    });
  }

  @POST("/{id}")
  @Override
  public CompletableFuture<Blog> createBlog(@Body Blog blog) {
    return CompletableFuture.supplyAsync(() -> {
      String id = validateBlogInfo(blog);
      blog.setId(id);
      blogMap.put(id, blog);
      log.info("Created blog: {}", blog);
      return blog;
    });
  }

  private String validateBlogInfo(@Body Blog blog) {
    String blogId = blog.getId() == null ? RandomStringUtils.randomAlphanumeric(6) : blog.getId();
    if (blogMap.containsKey(blogId)) {
      throw new DuplicateBlogException("Blog with ID " + blog.getId() + " already exists");
    }
    if (blog.getTitle() == null || blog.getTitle().isEmpty()) {
      throw new InvalidUserInputException("Title cannot be null or empty");
    }
    if (blog.getContent() == null || blog.getContent().isEmpty()) {
      throw new InvalidUserInputException("Content cannot be null or empty");
    }
    if (blog.getAuthor() == null) {
      throw new InvalidUserInputException("Author cannot be null");
    }
    try {
      User author = blog.getAuthor();
      userService.validateUser(author.getId(), author.getUsername()).join();
    } catch (CompletionException e) {
      if (e.getCause() instanceof BaseRpcException) {
        throw (BaseRpcException) e.getCause();
      }
      throw e;
    }
    return blogId;
  }

  @PUT("/{id}")
  @Override
  public CompletableFuture<Blog> updateBlog(@Path("id") String id, @Body Blog blog) {
    return CompletableFuture.supplyAsync(() -> {
      String blogId = validateBlogInfo(blog);
      Blog existingBlog = blogMap.get(id);
      if (!id.equals(blogId)) {
        throw new InvalidUserInputException("Blog ID in path and body do not match");
      }
      if (existingBlog == null) {
        log.warn("Blog not found with id: {}", id);
        throw new BlogNotFoundException("Blog not found, ID: " + id);
      }
      blogMap.put(id, blog);
      log.info("Updated blog: {}", blog);
      return blog;
    });
  }

  @DELETE("/{id}")
  @Override
  public CompletableFuture<Void> deleteBlog(@Path("id") String id) {
    return CompletableFuture.supplyAsync(() -> {
      Blog blog = blogMap.remove(id);
      if (blog == null) {
        log.warn("Blog not found with id: {}", id);
        throw new BlogNotFoundException("Blog not found, ID: " + id);
      }
      log.info("Deleted blog by id: {}", id);
      return null;
    });
  }

  @POST("/batch")
  @Override
  public CompletableFuture<List<Blog>> createBlogs(@Body List<Blog> blogs) {
    return CompletableFuture.supplyAsync(() -> {
      blogs.forEach(blog -> {
        String id = validateBlogInfo(blog);
        blog.setId(id);
        blogMap.put(id, blog);
        log.info("Created blog: {}", blog);
      });
      return blogs;
    });
  }

  @GET("/all")
  @Override
  public CompletableFuture<List<Blog>> selectAll() {
    return CompletableFuture.supplyAsync(() -> {
      log.info("Select all blogs");
      return new ArrayList<>(blogMap.values());
    });
  }
}
