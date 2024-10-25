package org.example.rpc.server.biz;

import org.example.rpc.api.User;
import org.example.rpc.api.UserService;
import org.example.rpc.core.annotations.RpcMethod;
import org.example.rpc.core.annotations.RpcService;
import org.apache.commons.lang3.RandomStringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RpcService
public class UserServiceImpl implements UserService {

  @RpcMethod
  @Override
  public CompletableFuture<User> selectById(String id) {
    return CompletableFuture.supplyAsync(() -> {
      User user = new User();
      user.setId(id);
      user.setUsername(RandomStringUtils.randomAlphabetic(5));
      log.info("Select user by id: {}", id);
      return user;
    });
  }
}
