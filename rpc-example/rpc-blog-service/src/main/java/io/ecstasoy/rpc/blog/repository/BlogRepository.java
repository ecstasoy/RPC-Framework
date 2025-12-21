package io.ecstasoy.rpc.blog.repository;

import lombok.RequiredArgsConstructor;
import io.ecstasoy.rpc.api.entity.BlogEntity;
import io.ecstasoy.rpc.blog.mapper.BlogMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BlogRepository {
  private final BlogMapper blogMapper;

  public void save(BlogEntity blog) {
    blogMapper.insert(blog);
  }

  public BlogEntity findById(String id) {
    return blogMapper.selectById(id);
  }

  public void update(BlogEntity blog) {
    blogMapper.update(blog);
  }

  public void deleteById(String id) {
    blogMapper.deleteById(id);
  }

  public List<BlogEntity> findAll() {
    return blogMapper.selectAll();
  }
}
