package org.example.rpc.api.service;

import org.example.rpc.api.dto.request.CreateBlogDTO;
import org.example.rpc.api.dto.request.UpdateBlogDTO;
import org.example.rpc.api.dto.response.BlogDTO;
import org.example.rpc.api.pojo.Blog;
import org.example.rpc.common.annotations.*;

import java.util.concurrent.CompletableFuture;
import java.util.List;

@Api("/blogs")
public interface BlogService {

  @GET("/{id}")
  CompletableFuture<BlogDTO> selectById(@Path("id") String id);

  @POST
  CompletableFuture<BlogDTO> createBlog(@Body CreateBlogDTO createBlogDTO);

  @PUT("/{id}")
  CompletableFuture<BlogDTO> updateBlog(@Path("id") String id, @Body UpdateBlogDTO updateBlogDTO);

  @DELETE("/{id}")
  CompletableFuture<Void> deleteBlog(@Path("id") String id);

  @POST("/batch")
  CompletableFuture<List<BlogDTO>> createBlogs(@Body List<CreateBlogDTO> createBlogDTOS);

  @GET("/all")
  CompletableFuture<List<BlogDTO>> selectAll();

}
