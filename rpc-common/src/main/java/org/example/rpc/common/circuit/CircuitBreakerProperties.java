package org.example.rpc.common.circuit;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Circuit breaker properties.
 *
 * <p>It is used to configure the circuit breaker.</p>
 *
 * @see CircuitBreaker
 * @see CircuitBreakerState
 * @author Kunhua Huang
 */
@Data
@Component
public class CircuitBreakerProperties {

  @Value("${rpc.circuit-breaker.failure-threshold:5}")
  private int failureThreshold;
  
  @Value("${rpc.circuit-breaker.reset-timeout-ms:1000}")
  private long resetTimeoutMs;
  
  @Value("${rpc.circuit-breaker.half-open-max-calls:10}")
  private int halfOpenMaxCalls;

  /**
   * Constructor.
   */
  public CircuitBreakerProperties() {
  }

  /**
   * Constructor.
   *
   * @param failureThreshold failure threshold
   * @param resetTimeoutMs reset timeout in milliseconds
   * @param halfOpenMaxCalls half open max calls
   */
  public CircuitBreakerProperties(int failureThreshold, long resetTimeoutMs, int halfOpenMaxCalls) {
    this.failureThreshold = failureThreshold;
    this.resetTimeoutMs = resetTimeoutMs;
    this.halfOpenMaxCalls = halfOpenMaxCalls;
  }
}
