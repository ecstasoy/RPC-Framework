package org.example.rpc.core.monitor.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.rpc.core.monitor.api.MonitoringService;
import org.example.rpc.core.monitor.model.MethodMetrics;
import org.example.rpc.core.monitor.model.StatisticalMetrics;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DefaultMonitoringServiceImpl implements MonitoringService {
  private final Map<String, List<MethodMetrics>> methodMetricsMap = new ConcurrentHashMap<>();

  @Override
  public void recordMetrics(String methodName, long executionTime, boolean success, String errorMessage) {
    methodMetricsMap.computeIfAbsent(methodName, k -> Collections.synchronizedList(new ArrayList<>()))
        .add(new MethodMetrics(methodName, executionTime, LocalDateTime.now(), success, errorMessage));
  }

  @Override
  public List<MethodMetrics> getMethodMetrics(String methodName) {
    return new ArrayList<>(methodMetricsMap.getOrDefault(methodName, Collections.emptyList()));
  }

  @Override
  public Map<String, StatisticalMetrics> getStatistics() {
    Map<String, StatisticalMetrics> stats = new HashMap<>();
    methodMetricsMap.forEach((method, metrics) -> {
      double avgTime = metrics.stream()
          .mapToLong(MethodMetrics::getExecutionTime)
          .average()
          .orElse(0.0);

      long maxTime = metrics.stream()
          .mapToLong(MethodMetrics::getExecutionTime)
          .max()
          .orElse(0);

      long successCount = metrics.stream()
          .filter(MethodMetrics::isSuccess)
          .count();

      long failureCount = metrics.size() - successCount; // 计算失败次数
      List<String> errorMessages = metrics.stream()
          .filter(m -> !m.isSuccess())
          .map(MethodMetrics::getErrorMessage)
          .collect(Collectors.toList()); // 收集错误信息
          
      stats.put(method, new StatisticalMetrics(avgTime, maxTime,
          successCount, metrics.size(), errorMessages));
    });
    return stats;
  }

  @Override
  public void clearHistoricalData(Duration retention) {
    LocalDateTime cutoffTime = LocalDateTime.now().minus(retention);
    methodMetricsMap.forEach((method, metrics) -> {
      metrics.removeIf(m -> m.getTimestamp().isBefore(cutoffTime));
    });
  }
}
