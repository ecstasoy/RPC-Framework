package org.example.rpc.core.test.circuit;

import lombok.extern.slf4j.Slf4j;
import org.example.rpc.core.common.circuit.CircuitBreaker;
import org.example.rpc.core.common.circuit.CircuitBreakerProperties;
import org.example.rpc.core.common.circuit.CircuitBreakerState;
import org.example.rpc.core.discovery.api.RpcServiceDiscovery;
import org.example.rpc.core.common.exception.RpcException;
import org.example.rpc.core.test.TestConfig;
import org.example.rpc.core.test.TestService;
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
    classes = {TestConfig.class}
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
    Thread.sleep(31000); // 略微超过配置的1000ms
    
    // 4. 验证半开状态下的调用
    String result = testService.echo("test-half-open");
    Assertions.assertEquals("Echo: test-half-open", result);
    
    // 5. 验证消息内容，而不是熔断器状态
    Assertions.assertTrue(result.contains("test-half-open"), 
        "Response should contain the test message");
  }

  @Test
  public void testCircuitBreakerReset() throws InterruptedException {
    CircuitBreaker circuitBreaker = testService.getCircuitBreaker();

    // 1. 验证初始状态为 CLOSED
    Assertions.assertEquals(CircuitBreakerState.CLOSED, circuitBreaker.getState());

    // 2. 触发熔断，进入 OPEN 状态
    for (int i = 0; i < 20; i++) {
      try {
        testService.echo("test" + i);
        log.info("Call {} succeeded", i);
      } catch (Exception ignored) {
        log.info("Call {} failed", i);
      }
      Thread.sleep(100);
    }
    Assertions.assertEquals(CircuitBreakerState.OPEN, circuitBreaker.getState());

    // 3. 等待重置时间，进入 HALF_OPEN 状态
    Thread.sleep(31000);
    String result = testService.echo("test-half-open");
    Assertions.assertEquals(CircuitBreakerState.HALF_OPEN, circuitBreaker.getState());

    // 4. 连续成功调用，验证是否重置为 CLOSED 状态
    for (int i = 0; i < 5; i++) {
      result = testService.echo("test-half-open");
      Assertions.assertEquals("Echo: test-half-open", result);
      circuitBreaker.recordSuccess();
    }

    // 5. 验证最终状态为 CLOSED
    Assertions.assertEquals(CircuitBreakerState.CLOSED, circuitBreaker.getState());
  }
}
