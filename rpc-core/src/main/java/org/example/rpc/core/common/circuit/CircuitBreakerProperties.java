package org.example.rpc.core.common.circuit;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Circuit breaker properties.
 */
@Data
@Component
public class CircuitBreakerProperties {

  @Value("${rpc.circuit-breaker.failure-threshold:5}")
  private int failureThreshold;

  @Value("${rpc.circuit-breaker.reset-timeout-ms:30000}")
  private long resetTimeoutMs;

  @Value("${rpc.circuit-breaker.half-open-max-calls:10}")
  private int halfOpenMaxCalls;
}