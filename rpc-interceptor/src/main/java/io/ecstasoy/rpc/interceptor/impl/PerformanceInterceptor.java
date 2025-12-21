package io.ecstasoy.rpc.interceptor.impl;

import lombok.extern.slf4j.Slf4j;
import io.ecstasoy.rpc.common.circuit.CircuitBreakerState;
import io.ecstasoy.rpc.common.enums.MetricType;
import io.ecstasoy.rpc.interceptor.RpcInterceptor;
import io.ecstasoy.rpc.monitor.api.MonitoringService;
import io.ecstasoy.rpc.monitor.model.MethodMetrics;
import io.ecstasoy.rpc.protocol.model.RpcRequest;
import io.ecstasoy.rpc.protocol.model.RpcResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Performance interceptor to record method metrics.
 *
 * <p>It records the metrics for the server-side.
 * <p>It records the duration of the method invocation, whether the method invocation is successful,
 * and the error message if the method invocation fails.
 * It also logs the duration of the method invocation and warns if the duration exceeds 1000ms.
 * <p>It implements the {@link RpcInterceptor} interface and overrides the three methods to record the metrics and log the duration.
 *
 * @see RpcInterceptor
 * @see MonitoringService
 * @see MethodMetrics
 * @author Kunhua Huang
 */
@Slf4j
@Component
public class PerformanceInterceptor implements RpcInterceptor {

  private final MonitoringService monitoringService;
  private final ThreadLocal<Long> startTime = new ThreadLocal<>();
  private final Map<String, List<MethodMetrics>> methodMetricsMap = new ConcurrentHashMap<>();

  /**
   * Instantiates a new Performance interceptor.
   *
   * <p>It is annotated with {@link org.springframework.beans.factory.annotation.Qualifier} to specify the monitoring service.
   *
   * @param monitoringService the monitoring service
   */
  public PerformanceInterceptor(@Qualifier("defaultMonitoringService") MonitoringService monitoringService) {
    this.monitoringService = monitoringService;
  }

  @Override
  public boolean preHandle(RpcRequest request) {
    startTime.set(System.currentTimeMillis());
    return false;
  }

  @Override
  public void postHandle(RpcRequest request, RpcResponse response, CircuitBreakerState state) {
    long duration = System.currentTimeMillis() - startTime.get();
    boolean success = response.getThrowable() == null;
    String errorMessage = success ? null : response.getThrowable().getMessage();
    MetricType metricType = success ? MetricType.NORMAL_REQUEST : MetricType.EXCEPTION;

    monitoringService.recordMetrics(
        request.getClassName() + "#" + request.getMethodName(),
        duration,
        success,
        errorMessage,
        metricType.toString()
    );

    if (!success) {
      log.error("Method [{}] failed with exception: {}", request.getMethodName(), errorMessage);
    }

    NumberFormat formatter = new DecimalFormat("#0.0000");
    log.debug("Method [{}] duration: {}ms", request.getMethodName(), formatter.format(duration));
    if (duration > 1000) {
      log.warn("Method [{}] duration too long: {}ms", request.getMethodName(), duration);
    }
  }

  @Override
  public void afterCompletion(RpcRequest request, RpcResponse response, Throwable ex) {
    startTime.remove();
  }
}
