package org.example.rpc.core.common.circuit;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Circuit breaker.
 */
@Slf4j
public class CircuitBreaker {
  private final AtomicReference<CircuitBreakerState> state;
  private final AtomicInteger failureCount;
  private final AtomicInteger successCount;
  private final int failureThreshold;
  private final long resetTimeoutMs;
  private final int halfOpenMaxCalls;
  private volatile long lastFailureTime;

  /**
   * Circuit breaker state.
   */
  public CircuitBreaker(CircuitBreakerProperties properties) {
    this.state = new AtomicReference<>(CircuitBreakerState.CLOSED);
    this.failureCount = new AtomicInteger(0);
    this.successCount = new AtomicInteger(0);
    this.failureThreshold = properties.getFailureThreshold();
    this.resetTimeoutMs = properties.getResetTimeoutMs();
    this.halfOpenMaxCalls = properties.getHalfOpenMaxCalls();
  }

  /**
   * Check if the request is allowed.
   * @return true if the request is allowed, otherwise false
   */
  public boolean allowRequest() {
    CircuitBreakerState currentState = state.get();
    
    if (currentState == CircuitBreakerState.CLOSED) {
        return true;
    }
    
    if (currentState == CircuitBreakerState.OPEN) {
        // 检查是否已经过了重置时间
        if (System.currentTimeMillis() - lastFailureTime >= resetTimeoutMs) {
            // 尝试切换到半开状态
            if (state.compareAndSet(CircuitBreakerState.OPEN, CircuitBreakerState.HALF_OPEN)) {
                successCount.set(0);  // 重置成功计数
                failureCount.set(0);  // 重置失败计数
                log.info("Circuit breaker state changed to HALF_OPEN");
            }
            return true;
        }
        return false;
    }
    
    if (currentState == CircuitBreakerState.HALF_OPEN) {
        return successCount.get() < halfOpenMaxCalls;
    }
    
    return false;  // 默认情况下不允许请求
  }

  /**
   * Record a successful call.
   */
  public void recordSuccess() {
    CircuitBreakerState currentState = state.get();
    if (currentState == CircuitBreakerState.HALF_OPEN) {
      int currentSuccess = successCount.incrementAndGet();
      if (currentSuccess >= halfOpenMaxCalls) {
        state.set(CircuitBreakerState.CLOSED);
        failureCount.set(0);
        successCount.set(0);
        log.info("Circuit breaker state changed to CLOSED");
      }
    }
  }

  /**
   * Record a failed call.
   */
  public void recordFailure() {
    lastFailureTime = System.currentTimeMillis();
    CircuitBreakerState currentState = state.get();

    if (currentState == CircuitBreakerState.HALF_OPEN) {
      state.set(CircuitBreakerState.OPEN);
      log.info("Circuit breaker state changed to OPEN");
    } else if (currentState == CircuitBreakerState.CLOSED) {
      if (failureCount.incrementAndGet() >= failureThreshold) {
        if (state.compareAndSet(CircuitBreakerState.CLOSED, CircuitBreakerState.OPEN)) {
          log.info("Circuit breaker state changed to OPEN");
        }
      }
    }
  }
}
