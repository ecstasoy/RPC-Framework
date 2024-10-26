package org.example.rpc.core.circuit;

/**
 * Circuit breaker state.
 */
public enum CircuitBreakerState {
    OPEN,
    HALF_OPEN,
    CLOSED
}
