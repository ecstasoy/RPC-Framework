package org.example.rpc.core.test.loadbalance;

import org.example.rpc.spring.annotation.RpcServiceScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.example.rpc.core.test.TestConfig;

@SpringBootApplication(scanBasePackages = "org.example.rpc.**")
@RpcServiceScan(basePackages = "org.example.rpc.**")
@Import(TestConfig.class)
public class LoadBalanceTestApplication {
  public static void main(String[] args) {
    SpringApplication.run(LoadBalanceTestApplication.class, args);
  }
}