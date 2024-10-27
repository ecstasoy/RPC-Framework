package org.example.rpc.core.monitor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;
@Data
@AllArgsConstructor
public class StatisticalMetrics {
  private double averageTime;
  private long maxTime;
  private long successCount;
  private long totalCount;
  private List<String> errorMessages;

  public double getSuccessRate() {
    return totalCount == 0 ? 0 : (double) successCount / totalCount;
  }
}
