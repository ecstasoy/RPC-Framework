package org.example.rpc.common.circuit;

/**
 * Circuit breaker state.
 *
 * <p> The circuit breaker has three states: OPEN, HALF_OPEN, and CLOSED.
 * <p> OPEN: The circuit breaker is open, and the request is not allowed to pass.
 * <p> HALF_OPEN: The circuit breaker is half-open, and the request is allowed to pass.
 * <p> CLOSED: The circuit breaker is closed, and the request is allowed to pass.
 *
 * @author Kunhua Huang
 */
public enum CircuitBreakerState {
  OPEN,
  HALF_OPEN,
  CLOSED
}
