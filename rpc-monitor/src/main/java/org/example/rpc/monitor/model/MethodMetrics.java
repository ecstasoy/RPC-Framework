package org.example.rpc.monitor.model;

import lombok.AllArgsConstructor;
import lombok.Data;

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
