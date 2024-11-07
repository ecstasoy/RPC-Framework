package org.example.rpc.blog.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.rpc.api.entity.BlogEntity;
import org.example.rpc.common.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface BlogMapper {
  void insert(BlogEntity blog);

  BlogEntity selectById(String id);

  void update(BlogEntity blog);

  void deleteById(String id);

  List<BlogEntity> selectAll();

  List<BlogEntity> selectByAuthorId(String authorId);

  BlogEntity selectByIdWithAuthor(@Param("id") String id);

  List<BlogEntity> selectAllWithAuthor();

  BlogEntity validateAuthor(String authorId);

  Map<String, String> validateAuthors(Set<String> authorIds);
}
