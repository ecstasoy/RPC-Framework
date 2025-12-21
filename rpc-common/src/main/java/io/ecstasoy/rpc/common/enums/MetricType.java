package io.ecstasoy.rpc.common.enums;

/**
 * Metric type for metrics collection.
 *
 * <p>There are four types of metrics:
 * <ul>
 *   <li>NORMAL_REQUEST: normal request</li>
 *   <li>CIRCUIT_BREAKER_REJECTED: circuit breaker rejected</li>
 *   <li>TIMEOUT: timeout</li>
 *   <li>EXCEPTION: exception</li>
 * </ul>
 *
 * @author Kunhua Huang
 */
public enum MetricType {
  NORMAL_REQUEST,
  CIRCUIT_BREAKER_REJECTED,
  TIMEOUT,
  EXCEPTION
}
