package io.ecstasoy.rpc.core.test;

import io.ecstasoy.rpc.common.annotations.RpcService;
import io.ecstasoy.rpc.common.circuit.CircuitBreaker;
import io.ecstasoy.rpc.common.circuit.CircuitBreakerProperties;
import io.ecstasoy.rpc.common.exception.RpcException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test service implementation.
 *
 * <p>Simulate a service that may fail randomly.
 *
 * @author Kunhua Huang
 */
@RpcService
@EnableConfigurationProperties({CircuitBreakerProperties.class})
public class TestServiceImpl implements TestService {
  private final AtomicInteger callCount = new AtomicInteger(0);
  private final CircuitBreaker circuitBreaker = new CircuitBreaker(new CircuitBreakerProperties());

  @Value("${netty.server.port}")
  private String port;

  @Value("${server.address}")
  private String ip;

  @Override
  public String echo(String message) {
    if (circuitBreaker.isCircuitbBreakerOpen()) {
      throw new RpcException("SERVICE_UNAVAILABLE",
          "Service is unavailable due to circuit breaker open", 503);
    }

    try {
      // Always return success for half-open test
      if (message.equals("test-half-open")) {
        circuitBreaker.recordSuccess();
        return "Echo: " + message;
      }

      // Every 3rd call will fail
      if (callCount.incrementAndGet() % 3 == 0) {
        throw new RpcException("SERVICE_UNAVAILABLE",
            "Service temporarily unavailable", 503);
      }
      circuitBreaker.recordSuccess();
      return "Echo: " + message;
    } catch (Exception e) {
      circuitBreaker.recordFailure();
      throw e;
    }
  }

  @Override
  public CircuitBreaker getCircuitBreaker() {
    return circuitBreaker;
  }
}
