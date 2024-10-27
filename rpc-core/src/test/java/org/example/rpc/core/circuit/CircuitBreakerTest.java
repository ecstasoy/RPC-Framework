package org.example.rpc.core.circuit;

import lombok.extern.slf4j.Slf4j;
import org.example.rpc.core.TestApplication;
import org.example.rpc.core.common.circuit.CircuitBreakerProperties;
import org.example.rpc.core.discovery.api.RpcServiceDiscovery;
import org.example.rpc.core.common.exception.RpcException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SuppressWarnings("ALL")
@Slf4j
@SpringBootTest(
    classes = {TestApplication.class, TestConfig.class},
    properties = {
        "rpc.circuit-breaker.failure-threshold=5",
        "rpc.circuit-breaker.reset-timeout-ms=1000",
        "rpc.circuit-breaker.half-open-max-calls=10"
    }
)
public class CircuitBreakerTest {
  @Autowired
  private CircuitBreakerProperties properties;

  @Autowired
  private TestService testService;

  @MockBean
  private RpcServiceDiscovery rpcServiceDiscovery;

  @BeforeEach
  public void setup() {
    when(rpcServiceDiscovery.getServiceInstance(anyString()))
        .thenReturn("localhost:8080");
  }

  @Test
  public void testCircuitBreaker() throws InterruptedException {
    // 1. 调用直到触发熔断（需要至少15次调用才能累积5次失败）
    for (int i = 0; i < 20; i++) {
      try {
        String result = testService.echo("test" + i);
        log.info("Call {} succeeded: {}", i, result);
      } catch (Exception e) {
        log.info("Call {} failed: {}", i, e.getMessage());
      }
      Thread.sleep(100); // 添加短暂延迟，避免调用过快
    }
    // 2. 验证熔断器已打开
    Exception exception = Assertions.assertThrows(RpcException.class, () -> {
      testService.echo("test-after-break");
    });
    log.info("Expected failure: {}", exception.getMessage());
    
    // 3. 等待重置时间
    Thread.sleep(1100); // 略微超过配置的1000ms
    
    // 4. 验证半开状态下的调用
    String result = testService.echo("test-half-open");
    Assertions.assertEquals("Echo: test-half-open", result);
    
    // 5. 验证消息内容，而不是熔断器状态
    Assertions.assertTrue(result.contains("test-half-open"), 
        "Response should contain the test message");
  }
}
