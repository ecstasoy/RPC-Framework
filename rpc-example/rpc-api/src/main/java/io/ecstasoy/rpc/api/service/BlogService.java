package io.ecstasoy.rpc.api.service;

import io.ecstasoy.rpc.api.dto.request.CreateBlogDTO;
import io.ecstasoy.rpc.api.dto.request.UpdateBlogDTO;
import io.ecstasoy.rpc.api.dto.response.BlogDTO;
import io.ecstasoy.rpc.common.annotations.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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
