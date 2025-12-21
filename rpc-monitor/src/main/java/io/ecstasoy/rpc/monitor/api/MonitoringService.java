package io.ecstasoy.rpc.monitor.api;

import io.ecstasoy.rpc.monitor.model.MethodMetrics;
import io.ecstasoy.rpc.monitor.model.StatisticalMetrics;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Monitoring service.
 *
 * <p>Record and get metrics.
 *
 * @author Kunhua Huang
 */
public interface MonitoringService {

  /**
   * Record metrics.
   *
   * @param methodName    method name
   * @param executionTime execution time
   * @param success       success or not
   * @param errorMessage  error message
   * @param metricType    metric type
   */
  void recordMetrics(String methodName, long executionTime, boolean success, String errorMessage, String metricType);

  /**
   * Get method metrics.
   *
   * @param methodName method name
   * @return method metrics
   */
  List<MethodMetrics> getMethodMetrics(String methodName);

  /**
   * Get statistics.
   *
   * @return statistics
   */
  Map<String, StatisticalMetrics> getStatistics();

  /**
   * Clear historical data.
   *
   * @param retention retention duration
   */
  void clearHistoricalData(Duration retention);
}
