package org.example.rpc.blog.convert;

import org.example.rpc.api.dto.request.CreateBlogDTO;
import org.example.rpc.api.dto.request.UpdateBlogDTO;
import org.example.rpc.api.dto.response.BlogDTO;
import org.example.rpc.api.entity.BlogEntity;
import org.springframework.stereotype.Component;

@Component
public class BlogConverter {

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

  public BlogEntity toBlogEntity(CreateBlogDTO dto) {
    BlogEntity entity = new BlogEntity();
    entity.setTitle(dto.getTitle());
    entity.setContent(dto.getContent());
    entity.setAuthorId(dto.getAuthorId());
    entity.setCreateTime(System.currentTimeMillis());
    entity.setUpdateTime(System.currentTimeMillis());
    return entity;
  }

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
