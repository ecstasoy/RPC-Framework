package io.ecstasoy.rpc.core.test.circuit;

import lombok.extern.slf4j.Slf4j;
import io.ecstasoy.rpc.common.circuit.CircuitBreaker;
import io.ecstasoy.rpc.common.circuit.CircuitBreakerProperties;
import io.ecstasoy.rpc.common.circuit.CircuitBreakerState;
import io.ecstasoy.rpc.common.exception.RpcException;
import io.ecstasoy.rpc.core.test.TestConfig;
import io.ecstasoy.rpc.core.test.TestService;
import io.ecstasoy.rpc.registry.discovery.api.RpcServiceDiscovery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.concurrent.ExecutionException;

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
  public void setup() throws ExecutionException {
    when(rpcServiceDiscovery.getServiceInstance(anyString()))
        .thenReturn("localhost:8080");
  }

  @Test
  public void testCircuitBreaker() throws InterruptedException {
    for (int i = 0; i < 50; i++) {
      try {
        String result = testService.echo("test" + i);
        log.info("Call {} succeeded: {}", i, result);
      } catch (Exception e) {
        log.info("Call {} failed: {}", i, e.getMessage());
      }
    }

    Exception exception = Assertions.assertThrows(RpcException.class, () -> {
      testService.echo("test-after-break");
    });
    log.info("Expected failure: {}", exception.getMessage());

    Thread.sleep(11000);

    String result = testService.echo("test-half-open");
    Assertions.assertEquals("Echo: test-half-open", result);

    Assertions.assertTrue(result.contains("test-half-open"), 
        "Response should contain the test message");
  }

  @Test
  public void testCircuitBreakerReset() throws InterruptedException {
    CircuitBreaker circuitBreaker = testService.getCircuitBreaker();

    Assertions.assertEquals(CircuitBreakerState.CLOSED, circuitBreaker.getState());

    for (int i = 0; i < 50; i++) {
      try {
        testService.echo("test" + i);
        log.info("Call {} succeeded", i);
      } catch (Exception ignored) {
        log.info("Call {} failed", i);
      }
    }
    Assertions.assertEquals(CircuitBreakerState.OPEN, circuitBreaker.getState());

    Thread.sleep(11000);
    String result = testService.echo("test-half-open");
    log.info("Response: {}", result);
    Assertions.assertEquals(CircuitBreakerState.HALF_OPEN, circuitBreaker.getState());

    for (int i = 0; i < 30; i++) {
      result = testService.echo("test-half-open");
      log.info("Response: {}", result);
      Assertions.assertEquals("Echo: test-half-open", result);
      circuitBreaker.recordSuccess();
    }

    Assertions.assertEquals(CircuitBreakerState.CLOSED, circuitBreaker.getState());
  }
}
