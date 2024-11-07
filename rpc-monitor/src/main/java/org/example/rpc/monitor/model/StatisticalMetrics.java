package org.example.rpc.monitor.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * <p>Statistical metrics.
 *
 * <p>Provide statistical metrics for monitoring.
 * <p>It includes average time, max time, success count, total count, success rate, error messages, and metric types.
 *
 * @author Kunhua Huang
 */
@Data
@AllArgsConstructor
public class StatisticalMetrics {
  private double averageTime;
  private long maxTime;
  private long successCount;
  private long totalCount;
  private double successRate;
  private List<String> errorMessages;
  private Map<String, Integer> metricTypes;
}
