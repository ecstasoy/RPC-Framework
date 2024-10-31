package org.example.rpc.api.service;

import org.example.rpc.api.pojo.Blog;
import org.example.rpc.core.common.annotations.*;

import java.util.concurrent.CompletableFuture;
import java.util.List;

@Api("/blogs")
public interface BlogService {

  @GET("/{id}")
  CompletableFuture<Blog> selectById(@Path("id") String id);

  @POST
  CompletableFuture<Blog> createBlog(@Body Blog blog);

  @PUT("/{id}")
  CompletableFuture<Blog> updateBlog(@Path("id") String id, @Body Blog blog);

  @DELETE("/{id}")
  CompletableFuture<Void> deleteBlog(@Path("id") String id);

  @POST("/batch")
  CompletableFuture<List<Blog>> createBlogs(@Body List<Blog> blogs);

  @GET("/all")
  CompletableFuture<List<Blog>> selectAll();

}
