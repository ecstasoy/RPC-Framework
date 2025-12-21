package io.ecstasoy.rpc.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class UserServiceConfig {

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
