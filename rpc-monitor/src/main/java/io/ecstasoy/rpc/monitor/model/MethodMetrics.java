package io.ecstasoy.rpc.monitor.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Method metrics.
 *
 * <p>It is used to store the metrics of a method.
 *
 * @author Kunhua Huang
 */
@Data
@AllArgsConstructor
public class MethodMetrics {
  private String methodName;
  private long executionTime;
  private LocalDateTime timestamp;
  private boolean success;
  private String errorMessage;
  private String metricType;
}
