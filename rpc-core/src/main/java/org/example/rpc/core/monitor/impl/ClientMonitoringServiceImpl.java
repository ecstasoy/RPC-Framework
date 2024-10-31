package org.example.rpc.core.monitor.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.rpc.common.circuit.CircuitBreakerState;
import org.example.rpc.core.monitor.api.MonitoringService;
import org.example.rpc.core.monitor.model.MethodMetrics;
import org.example.rpc.core.monitor.model.StatisticalMetrics;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service("clientMonitoringService")
public class ClientMonitoringServiceImpl implements MonitoringService {
  private final Map<String, List<MethodMetrics>> methodMetricsMap = new ConcurrentHashMap<>();
  private final Map<String, CircuitBreakerState> lastCircuitBreakerStates = new ConcurrentHashMap<>();

  @Override
  public void recordMetrics(String methodName, long executionTime, boolean success, String errorMessage, String metricType) {

    if (!success && errorMessage != null && errorMessage.contains("circuit breaker")) {
      methodMetricsMap.computeIfAbsent(methodName, k -> Collections.synchronizedList(new ArrayList<>()))
          .add(new MethodMetrics(methodName, executionTime, LocalDateTime.now(), false, errorMessage, metricType));
        lastCircuitBreakerStates.put(methodName, CircuitBreakerState.OPEN);
    }
    if (success) {
      methodMetricsMap.computeIfAbsent(methodName, k -> Collections.synchronizedList(new ArrayList<>()))
          .add(new MethodMetrics(methodName, executionTime, LocalDateTime.now(), true, null, metricType));
      if (metricType == "HALF_OPEN") {
        lastCircuitBreakerStates.put(methodName, CircuitBreakerState.HALF_OPEN);
      } else {
        lastCircuitBreakerStates.put(methodName, CircuitBreakerState.CLOSED);
      }
    }
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

      long failureCount = metrics.stream()
          .filter(m -> !m.isSuccess())
          .count();

      long totalCount = metrics.size();

      long successCount = totalCount - failureCount;

      double successRate = totalCount == 0 ? 0 : (double) successCount / totalCount;

      List<String> errorMessages = metrics.stream()
          .filter(m -> !m.isSuccess())
          .map(MethodMetrics::getErrorMessage)
          .distinct()
          .collect(Collectors.toList());

      Map<String, Integer> metricType = metrics.stream()
          .collect(Collectors.groupingBy(MethodMetrics::getMetricType, Collectors.summingInt(e -> 1)));

      StatisticalMetrics serviceStats = new StatisticalMetrics(
          avgTime,
          maxTime,
          successCount,
          totalCount,
          successRate,
          errorMessages,
          metricType
      );

      stats.put(method, serviceStats);
    });
    return stats;
  }

  @Override
  public void clearHistoricalData(Duration retention) {
    LocalDateTime cutoffTime = LocalDateTime.now().minus(retention);
    methodMetricsMap.forEach((method, metrics) -> {
      metrics.removeIf(metric -> metric.getTimestamp().isBefore(cutoffTime));
      if (metrics.isEmpty()) {
        methodMetricsMap.remove(method);
        lastCircuitBreakerStates.remove(method);
      }
    });
  }

  public CircuitBreakerState getCurrentState(String methodName) {
    return lastCircuitBreakerStates.getOrDefault(methodName, CircuitBreakerState.CLOSED);
  }
}