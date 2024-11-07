package org.example.rpc.user.repository;

import lombok.RequiredArgsConstructor;
import org.example.rpc.api.entity.UserEntity;
import org.example.rpc.user.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepository {
  private final UserMapper userMapper;

  public void save(UserEntity user) {
    userMapper.insert(user);
  }

  public UserEntity findById(String id) {
    return userMapper.selectById(id);
  }

  public void update(UserEntity user) {
    userMapper.update(user);
  }

  public void deleteById(String id) {
    userMapper.deleteById(id);
  }

  public List<UserEntity> findAll() {
    return userMapper.selectAll();
  }

  public UserEntity findByUsername(String username) {
    return userMapper.selectByUsername(username);
  }

  public UserEntity findByEmail(String email) {
    return userMapper.selectByEmail(email);
  }
}
