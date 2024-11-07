package org.example.rpc.blog.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.example.rpc.api.dto.request.CreateBlogDTO;
import org.example.rpc.api.dto.request.UpdateBlogDTO;
import org.example.rpc.api.dto.response.BlogDTO;
import org.example.rpc.api.entity.BlogEntity;
import org.example.rpc.api.exception.blog.BlogNotFoundException;
import org.example.rpc.api.exception.blog.DuplicateBlogException;
import org.example.rpc.api.service.BlogService;
import org.example.rpc.blog.convert.BlogConverter;
import org.example.rpc.blog.repository.BlogRepository;
import org.example.rpc.common.annotations.*;
import org.example.rpc.common.exception.RpcException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@RpcService
public class BlogServiceImpl implements BlogService {

  private final BlogRepository blogRepository;
  private final BlogConverter blogConverter;

  public BlogServiceImpl(BlogRepository blogRepository, BlogConverter blogConverter) {
    this.blogRepository = blogRepository;
    this.blogConverter = blogConverter;
  }

  @GET("/{id}")
  @Override
  public CompletableFuture<BlogDTO> selectById(@Path("id") String id) {
    return CompletableFuture.supplyAsync(() -> {
      log.debug("Selecting blog by id: {}", id);
      BlogEntity blogEntity = blogRepository.findByIdWithAuthor(id);
      if (blogEntity == null) {
        log.warn("Blog not found with id: {}", id);
        throw new BlogNotFoundException("Blog not found, ID: " + id);
      }
      log.info("Select blog by id: {}", id);
      BlogDTO blogDTO = blogConverter.toBlogDTO(blogEntity);
      return blogDTO;
    });
  }

  @POST
  @Override
  public CompletableFuture<BlogDTO> createBlog(@Body CreateBlogDTO createBlogDTO) {
    return CompletableFuture.supplyAsync(() -> {
      validateCreateBlogDTO(createBlogDTO);

      BlogEntity authorInfo = blogRepository.validateAuthor(createBlogDTO.getAuthorId());
      if (authorInfo == null) {
        throw new RpcException("AUTHOR_NOT_FOUND",
            "Author not found with ID: " + createBlogDTO.getAuthorId(),
            HttpStatus.NOT_FOUND.value());
      }

      BlogEntity blogEntity = blogConverter.toBlogEntity(createBlogDTO);
      String blogId = RandomStringUtils.randomAlphanumeric(6);
      blogEntity.setId(blogId);
      blogEntity.setCreateTime(System.currentTimeMillis());
      blogEntity.setUpdateTime(System.currentTimeMillis());
      blogEntity.setAuthorName(authorInfo.getAuthorName());  // 设置作者名

      blogRepository.save(blogEntity);
      return blogConverter.toBlogDTO(blogEntity);
    });
  }

  private void validateCreateBlogDTO(CreateBlogDTO createBlogDTO) {
    if (createBlogDTO.getId() != null &&
        blogRepository.findById(createBlogDTO.getId()) != null) {
      throw new DuplicateBlogException(
          "Blog with ID " + createBlogDTO.getId() + " already exists");
    }
  }

  private void validateUpdateBlogDTO(String id, UpdateBlogDTO dto) {
    BlogEntity existingBlog = blogRepository.findById(id);
    if (existingBlog == null) {
      throw new BlogNotFoundException("Blog not found, ID: " + id);
    }
  }

  @PUT("/{id}")
  @Override
  public CompletableFuture<BlogDTO> updateBlog(@Path("id") String id, @Body UpdateBlogDTO updateBlogDTO) {
    return CompletableFuture.supplyAsync(() -> {
      validateUpdateBlogDTO(id, updateBlogDTO);
      BlogEntity blogEntity = blogRepository.findByIdWithAuthor(id);
      blogConverter.updateBlog(updateBlogDTO, blogEntity);
      blogRepository.update(blogEntity);
      log.info("Updated blog: {}", blogEntity);
      BlogDTO blogDTO = blogConverter.toBlogDTO(blogEntity);
      return blogDTO;
    });
  }

  @DELETE("/{id}")
  @Override
  public CompletableFuture<Void> deleteBlog(@Path("id") String id) {
    return CompletableFuture.supplyAsync(() -> {
      BlogEntity blogEntity = blogRepository.findById(id);
      if (blogEntity == null) {
        throw new BlogNotFoundException("Blog not found, ID: " + id);
      }
      blogRepository.deleteById(id);
      log.info("Deleted blog with id: {}", id);
      return null;
    });
  }

  @POST("/batch")
  @Override
  @Transactional
  public CompletableFuture<List<BlogDTO>> createBlogs(@Body List<CreateBlogDTO> createBlogDTOs) {
    return CompletableFuture.supplyAsync(() -> {

      Set<String> authorIds = createBlogDTOs.stream()
          .map(CreateBlogDTO::getAuthorId)
          .collect(Collectors.toSet());

      Map<String, String> authorNames = blogRepository.validateAuthors(authorIds);
      Set<String> existingAuthorIds = authorNames.keySet();

      Set<String> notFoundAuthors = authorIds.stream()
          .filter(id -> !existingAuthorIds.contains(id))
          .collect(Collectors.toSet());

      if (!notFoundAuthors.isEmpty()) {
        throw new RpcException("AUTHOR_NOT_FOUND",
            "Authors not found with IDs: " + notFoundAuthors,
            HttpStatus.NOT_FOUND.value());
      }

      List<BlogDTO> createdBlogs = new ArrayList<>();
      for (CreateBlogDTO dto : createBlogDTOs) {
        BlogEntity blogEntity = blogConverter.toBlogEntity(dto);
        String blogId = RandomStringUtils.randomAlphanumeric(6);
        blogEntity.setId(blogId);
        blogEntity.setCreateTime(System.currentTimeMillis());
        blogEntity.setUpdateTime(System.currentTimeMillis());
        blogEntity.setAuthorName(authorNames.get(dto.getAuthorId()));

        blogRepository.save(blogEntity);
        createdBlogs.add(blogConverter.toBlogDTO(blogEntity));
      }
      return createdBlogs;
    });
  }

  @GET("/all")
  @Override
  public CompletableFuture<List<BlogDTO>> selectAll() {
    return CompletableFuture.supplyAsync(() -> {
      log.info("Select all blogs");
      List<BlogEntity> blogEntities = blogRepository.findAllWithAuthor();
      return blogEntities.stream()
          .map(blogConverter::toBlogDTO)
          .collect(Collectors.toList());
    });
  }
}
