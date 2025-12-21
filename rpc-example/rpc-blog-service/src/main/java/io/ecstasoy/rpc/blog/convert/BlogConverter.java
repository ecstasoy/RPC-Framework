package io.ecstasoy.rpc.blog.convert;

import io.ecstasoy.rpc.api.dto.request.CreateBlogDTO;
import io.ecstasoy.rpc.api.dto.request.UpdateBlogDTO;
import io.ecstasoy.rpc.api.dto.response.BlogDTO;
import io.ecstasoy.rpc.api.entity.BlogEntity;
import org.springframework.stereotype.Component;

/**
 * Blog converter.
 */
@Component
public class BlogConverter {

  /**
   * Convert BlogEntity to BlogDTO.
   *
   * @param entity Blog entity
   * @return Blog DTO
   */
  public BlogDTO toBlogDTO(BlogEntity entity) {
    BlogDTO dto = new BlogDTO();
    dto.setId(entity.getId());
    dto.setTitle(entity.getTitle());
    dto.setContent(entity.getContent());
    dto.setAuthorId(entity.getAuthorId());
    dto.setCreateTime(entity.getCreateTime());
    dto.setUpdateTime(entity.getUpdateTime());
    return dto;
  }

  /**
   * Convert CreateBlogDTO to BlogEntity.
   *
   * @param dto Create blog DTO
   * @return Blog entity
   */
  public BlogEntity toBlogEntity(CreateBlogDTO dto) {
    BlogEntity entity = new BlogEntity();
    entity.setTitle(dto.getTitle());
    entity.setContent(dto.getContent());
    entity.setAuthorId(dto.getAuthorId());
    entity.setCreateTime(System.currentTimeMillis());
    entity.setUpdateTime(System.currentTimeMillis());
    return entity;
  }

  /**
   * Update BlogEntity with UpdateBlogDTO.
   *
   * @param dto        Update blog DTO
   * @param blogEntity Blog entity
   */
  public void updateBlog(UpdateBlogDTO dto, BlogEntity blogEntity) {
    if (dto.getTitle() != null) {
      blogEntity.setTitle(dto.getTitle());
    }
    if (dto.getContent() != null) {
      blogEntity.setContent(dto.getContent());
    }
    blogEntity.setUpdateTime(System.currentTimeMillis());
  }
}
