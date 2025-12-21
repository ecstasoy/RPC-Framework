package io.ecstasoy.rpc.core.test.loadbalance;

import io.ecstasoy.rpc.core.test.TestConfig;
import io.ecstasoy.rpc.spring.annotation.RpcServiceScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = "io.ecstasoy.rpc.**")
@RpcServiceScan(basePackages = "io.ecstasoy.rpc.**")
@Import(TestConfig.class)
public class LoadBalanceTestApplication {
  public static void main(String[] args) {
    SpringApplication.run(LoadBalanceTestApplication.class, args);
  }
}