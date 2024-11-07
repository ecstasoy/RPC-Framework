package org.example.rpc.common.circuit;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Circuit breaker.
 *
 * <p>When the failure count reaches the threshold, the circuit breaker will be opened and all requests will be rejected.
 * After the reset timeout, the circuit breaker will be half-opened and allow a limited number of requests to pass.
 * If the requests are successful, the circuit breaker will be closed again. Otherwise, it will be opened again.
 *
 * @see CircuitBreakerState
 * @see CircuitBreakerProperties
 * @author Kunhua Huang
 */
@Slf4j
public class CircuitBreaker {

  private final CircuitBreakerProperties properties;
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
    this.properties = properties;
    this.state = new AtomicReference<>(CircuitBreakerState.CLOSED);
    this.failureCount = new AtomicInteger(0);
    this.successCount = new AtomicInteger(0);
    this.failureThreshold = properties.getFailureThreshold();
    this.resetTimeoutMs = properties.getResetTimeoutMs();
    this.halfOpenMaxCalls = properties.getHalfOpenMaxCalls();
  }

  /**
   * Check if the request is allowed.
   *
   * @return true if the request is allowed, otherwise false
   */
  public boolean isCircuitbBreakerOpen() {
    CircuitBreakerState currentState = state.get();

    if (currentState == CircuitBreakerState.CLOSED) {
      return false;
    }

    if (currentState == CircuitBreakerState.OPEN) {

      long currentTime = System.currentTimeMillis();
      if (currentTime - lastFailureTime >= resetTimeoutMs) {

        if (state.compareAndSet(CircuitBreakerState.OPEN, CircuitBreakerState.HALF_OPEN)) {
          log.info("Circuit breaker state changed to HALF_OPEN");
          successCount.set(0);
          return false;
        }
      }
      return true;
    }

    // HALF_OPEN 状态下限制并发请求数
    return successCount.get() >= halfOpenMaxCalls;
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
      if (state.compareAndSet(CircuitBreakerState.HALF_OPEN, CircuitBreakerState.OPEN)) {
        log.info("Circuit breaker state changed to OPEN");
        failureCount.set(0);
        successCount.set(0);
      }
    } else if (currentState == CircuitBreakerState.CLOSED) {
      if (failureCount.incrementAndGet() >= failureThreshold) {
        if (state.compareAndSet(CircuitBreakerState.CLOSED, CircuitBreakerState.OPEN)) {
          log.info("Circuit breaker state changed to OPEN");
          failureCount.set(0);
          successCount.set(0);
        }
      }
    }
  }

  public CircuitBreakerState getState() {
    return state.get();
  }

  public void reset() {
    state.set(CircuitBreakerState.CLOSED);
    failureCount.set(0);
    successCount.set(0);
    lastFailureTime = 0;
    log.info("Circuit breaker manually reset to CLOSED state");
  }
}
