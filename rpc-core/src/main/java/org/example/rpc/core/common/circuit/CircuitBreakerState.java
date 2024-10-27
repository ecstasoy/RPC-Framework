package org.example.rpc.core.common.circuit;

/**
 * Circuit breaker state.
 */
public enum CircuitBreakerState {
    OPEN,
    HALF_OPEN,
    CLOSED
}
