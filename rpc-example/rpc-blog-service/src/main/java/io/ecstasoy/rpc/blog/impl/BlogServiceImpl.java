package io.ecstasoy.rpc.blog.impl;

import io.ecstasoy.rpc.common.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import io.ecstasoy.rpc.api.dto.request.CreateBlogDTO;
import io.ecstasoy.rpc.api.dto.request.UpdateBlogDTO;
import io.ecstasoy.rpc.api.dto.response.BlogDTO;
import io.ecstasoy.rpc.api.dto.response.UserDTO;
import io.ecstasoy.rpc.api.entity.BlogEntity;
import io.ecstasoy.rpc.api.exception.blog.BlogNotFoundException;
import io.ecstasoy.rpc.api.exception.blog.DuplicateBlogException;
import io.ecstasoy.rpc.api.service.BlogService;
import io.ecstasoy.rpc.api.service.UserService;
import io.ecstasoy.rpc.blog.convert.BlogConverter;
import io.ecstasoy.rpc.blog.repository.BlogRepository;
import io.ecstasoy.rpc.common.exception.RpcException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RpcService
@Service
public class BlogServiceImpl implements BlogService {
  private final BlogRepository blogRepository;
  private final BlogConverter blogConverter;
  private final UserService userService;

  @Autowired
  public BlogServiceImpl(BlogRepository blogRepository,
                         BlogConverter blogConverter,
                         @Reference UserService userService) {
    this.blogRepository = blogRepository;
    this.blogConverter = blogConverter;
    this.userService = userService;
  }

  @GET("/{id}")
  @Override
  public CompletableFuture<BlogDTO> selectById(@Path("id") String id) {
    return CompletableFuture.supplyAsync(() -> {
      log.debug("Selecting blog by id: {}", id);
      BlogEntity blogEntity = blogRepository.findById(id);
      if (blogEntity == null) {
        log.warn("Blog not found with id: {}", id);
        throw new BlogNotFoundException("Blog not found, ID: " + id);
      }
      log.info("Select blog by id: {}", id);
      BlogDTO blogDTO = blogConverter.toBlogDTO(blogEntity);
      try {
        userService.selectById(blogEntity.getAuthorId())
            .thenAccept(userDTO -> blogDTO.setAuthorName(userDTO.getUsername()))
            .get(3, TimeUnit.SECONDS);
        return blogDTO;
      } catch (Exception e) {
        log.error("Failed to get author details", e);
        throw new RuntimeException("Failed to get author details", e);
      }
    });
  }

  @POST
  @Override
  public CompletableFuture<BlogDTO> createBlog(@Body CreateBlogDTO createBlogDTO) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        validateCreateBlogDTO(createBlogDTO);

        UserDTO authorInfo = userService.selectById(createBlogDTO.getAuthorId())
            .get(3, TimeUnit.SECONDS);

        if (authorInfo == null) {
          throw new RpcException("AUTHOR_NOT_FOUND", "Author not found with ID: " + createBlogDTO.getAuthorId(), HttpStatus.NOT_FOUND.value());
        }

        BlogEntity blogEntity = blogConverter.toBlogEntity(createBlogDTO);
        String blogId = RandomStringUtils.randomAlphanumeric(6);
        blogEntity.setId(blogId);
        blogEntity.setCreateTime(System.currentTimeMillis());
        blogEntity.setUpdateTime(System.currentTimeMillis());
        blogEntity.setAuthorId(createBlogDTO.getAuthorId());

        blogRepository.save(blogEntity);

        BlogDTO blogDTO = blogConverter.toBlogDTO(blogEntity);
        blogDTO.setAuthorName(authorInfo.getUsername());
        return blogDTO;

      } catch (Exception e) {
        log.error("Failed to create blog", e);
        throw new BlogNotFoundException(e.getMessage());
      }
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
      BlogEntity blogEntity = blogRepository.findById(id);
      blogConverter.updateBlog(updateBlogDTO, blogEntity);
      blogRepository.update(blogEntity);
      log.info("Updated blog: {}", blogEntity);
      BlogDTO blogDTO = blogConverter.toBlogDTO(blogEntity);
      try {
        userService.selectById(blogEntity.getAuthorId())
            .thenAccept(userDTO -> blogDTO.setAuthorName(userDTO.getUsername()))
            .get(3, TimeUnit.SECONDS);
      } catch (Exception e) {
        log.error("Failed to get author details", e);
        throw new RuntimeException("Failed to get author details", e);
      }
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
      try {
        List<CompletableFuture<UserDTO>> futures = authorIds.stream()
            .map(userService::selectById)
            .collect(Collectors.toList());

        Map<String, String> authorIdToName = futures.stream()
            .map(future -> {
              try {
                return future.get(3, TimeUnit.SECONDS);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            })
            .collect(Collectors.toMap(
                UserDTO::getId,
                UserDTO::getUsername
            ));

        // 创建博客
        List<BlogDTO> createdBlogs = new ArrayList<>();
        for (CreateBlogDTO dto : createBlogDTOs) {
          BlogEntity blogEntity = blogConverter.toBlogEntity(dto);
          String blogId = RandomStringUtils.randomAlphanumeric(6);
          blogEntity.setId(blogId);
          blogEntity.setCreateTime(System.currentTimeMillis());
          blogEntity.setUpdateTime(System.currentTimeMillis());

          blogRepository.save(blogEntity);
          createdBlogs.add(blogConverter.toBlogDTO(blogEntity));
        }
        return createdBlogs;

      } catch (Exception e) {
        log.error("Failed to validate authors", e);
        throw new RpcException("AUTHOR_VALIDATION_FAILED",
            "Failed to validate authors: " + e.getMessage(),
            HttpStatus.BAD_REQUEST.value());
      }
    });
  }

  @GET("/all")
  @Override
  public CompletableFuture<List<BlogDTO>> selectAll() {
    return CompletableFuture.supplyAsync(() -> {
      log.info("Select all blogs");
      List<BlogEntity> blogEntities = blogRepository.findAll();
      return blogEntities.stream()
          .map(blogConverter::toBlogDTO)
          .collect(Collectors.toList());
    });
  }
}
