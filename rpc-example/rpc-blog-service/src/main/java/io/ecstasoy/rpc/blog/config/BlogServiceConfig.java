package io.ecstasoy.rpc.blog.config;

import io.ecstasoy.rpc.api.service.UserService;
import io.ecstasoy.rpc.network.RpcRequestSender;
import io.ecstasoy.rpc.proxy.RpcClientProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Blog Service Configuration.
 */
@Configuration
public class BlogServiceConfig {

  /**
   * Bean definition for UserService.
   */
  @Bean
  public UserService userService(RpcRequestSender requestSender) {
    RpcClientProxy clientProxy = new RpcClientProxy(requestSender);
    return clientProxy.getProxy(UserService.class);
  }

  /**
   * Bean definition for async executor.
   */
  @Bean
  public Executor asyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(100);
    executor.setMaxPoolSize(300);
    executor.setQueueCapacity(1500);
    executor.setKeepAliveSeconds(60);
    executor.setThreadNamePrefix("Async-");
    executor.initialize();
    return executor;
  }
}
