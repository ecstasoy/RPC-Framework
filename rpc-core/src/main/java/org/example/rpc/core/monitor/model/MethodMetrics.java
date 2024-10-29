package org.example.rpc.core.monitor.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

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
