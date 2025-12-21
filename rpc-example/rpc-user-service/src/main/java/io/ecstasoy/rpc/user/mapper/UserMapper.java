package io.ecstasoy.rpc.user.mapper;

import org.apache.ibatis.annotations.*;
import io.ecstasoy.rpc.api.entity.UserEntity;

import java.util.List;

@Mapper
public interface UserMapper {
  @Select("SELECT * FROM users WHERE username = #{username}")
  UserEntity selectByUsername(String username);

  @Select("SELECT * FROM users WHERE email = #{email}")
  UserEntity selectByEmail(String email);

  @Insert("INSERT INTO users (id, username, password, email, create_time, update_time) "
      + "VALUES (#{id}, #{username}, #{password}, #{email}, #{createTime}, #{updateTime})")
  void insert(UserEntity user);

  @Select("SELECT * FROM users WHERE id = #{id}")
  UserEntity selectById(String id);

  @Update("UPDATE users SET username=#{username}, password=#{password}, "
      + "email=#{email}, update_time=#{updateTime} WHERE id=#{id}")
  void update(UserEntity user);

  @Delete("DELETE FROM users WHERE id = #{id}")
  void deleteById(String id);

  @Select("SELECT * FROM users")
  List<UserEntity> selectAll();
}
