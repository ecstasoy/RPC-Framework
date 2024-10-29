package org.example.rpc.core.monitor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;
import java.util.Map;

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
