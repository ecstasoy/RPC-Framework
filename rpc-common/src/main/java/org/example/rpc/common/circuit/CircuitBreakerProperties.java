package org.example.rpc.common.circuit;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
@ConfigurationProperties(prefix = "rpc.circuit-breaker")
public class CircuitBreakerProperties {

  private int failureThreshold = 15;
  private long resetTimeoutMs = 10000;
  private int halfOpenMaxCalls = 30;

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
