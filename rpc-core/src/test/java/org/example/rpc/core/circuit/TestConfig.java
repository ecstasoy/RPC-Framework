package org.example.rpc.core.circuit;

import org.example.rpc.core.common.circuit.CircuitBreakerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfig {

  @Bean
  public TestService testService(CircuitBreakerProperties properties) {
    return new TestServiceImpl(properties);
  }
}