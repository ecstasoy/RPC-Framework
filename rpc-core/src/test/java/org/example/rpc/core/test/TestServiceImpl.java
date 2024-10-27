package org.example.rpc.core.test;

import org.example.rpc.core.common.annotations.RpcService;
import org.example.rpc.core.common.circuit.CircuitBreaker;
import org.example.rpc.core.common.circuit.CircuitBreakerProperties;
import org.example.rpc.core.common.exception.RpcException;

import java.util.concurrent.atomic.AtomicInteger;

@RpcService
public class TestServiceImpl implements TestService {
  private final AtomicInteger callCount = new AtomicInteger(0);
  private final CircuitBreaker circuitBreaker;

  public TestServiceImpl(CircuitBreakerProperties properties) {
    this.circuitBreaker = new CircuitBreaker(properties);
  }

  @Override
  public String echo(String message) {
    if (!circuitBreaker.allowRequest()) {
      throw new RpcException("SERVICE_UNAVAILABLE",
          "Service is unavailable due to circuit breaker open", 503);
    }

    try {
      // 在半开状态下，始终返回成功
      if (message.equals("test-half-open")) {
        circuitBreaker.recordSuccess();
        return "Echo: " + message;
      }

      // 每连续3次调用中的第3次必定失败
      if (callCount.incrementAndGet() % 3 == 0) {
        circuitBreaker.recordFailure();
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
}
