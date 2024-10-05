package org.example.rpc.server.biz;

import org.example.rpc.api.User;
import org.example.rpc.api.UserService;
import org.example.rpc.core.annotations.RpcService;
import org.apache.commons.lang3.RandomStringUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RpcService
public class UserServiceImpl implements UserService {

  @Override
  public User selectById(String id) {
    User user = new User();
    user.setId(id);
    user.setUsername(RandomStringUtils.randomAlphabetic(5));
    log.info("Select user by id: {}", id);
    return user;
  }
}
