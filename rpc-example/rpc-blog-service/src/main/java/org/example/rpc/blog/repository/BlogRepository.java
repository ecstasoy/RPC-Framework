package org.example.rpc.blog.repository;

import lombok.RequiredArgsConstructor;
import org.example.rpc.api.entity.BlogEntity;
import org.example.rpc.blog.mapper.BlogMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

  public BlogEntity findByIdWithAuthor(String id) {
    return blogMapper.selectByIdWithAuthor(id);
  }

  public List<BlogEntity> findAllWithAuthor() {
    return blogMapper.selectAllWithAuthor();
  }

  public BlogEntity validateAuthor(String authorId) {
    return blogMapper.validateAuthor(authorId);
  }

  public Map<String, String> validateAuthors(Set<String> authorIds) {
    return blogMapper.validateAuthors(authorIds);
  }

}
