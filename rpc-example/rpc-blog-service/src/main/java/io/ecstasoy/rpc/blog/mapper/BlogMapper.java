package io.ecstasoy.rpc.blog.mapper;

import org.apache.ibatis.annotations.Mapper;
import io.ecstasoy.rpc.api.entity.BlogEntity;

import java.util.List;

@Mapper
public interface BlogMapper {
  void insert(BlogEntity blog);

  BlogEntity selectById(String id);

  void update(BlogEntity blog);

  void deleteById(String id);

  List<BlogEntity> selectAll();
}
