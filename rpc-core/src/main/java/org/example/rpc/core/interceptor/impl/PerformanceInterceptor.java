package org.example.rpc.core.interceptor.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.rpc.core.interceptor.RpcInterceptor;
import org.example.rpc.core.model.RpcRequest;
import org.example.rpc.core.model.RpcResponse;
import org.example.rpc.core.monitor.api.MonitoringService;
import org.example.rpc.core.monitor.model.MethodMetrics;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class PerformanceInterceptor implements RpcInterceptor {
  private final MonitoringService monitoringService;
  private final ThreadLocal<Long> startTime = new ThreadLocal<>();
  private final Map<String, List<MethodMetrics>> methodMetricsMap = new ConcurrentHashMap<>();

  public PerformanceInterceptor(MonitoringService monitoringService) {
    this.monitoringService = monitoringService;
  }

  @Override
  public boolean preHandle(RpcRequest request) {
    startTime.set(System.currentTimeMillis());
    return true;
  }

  @Override
  public void postHandle(RpcRequest request, RpcResponse response) {
    long duration = System.currentTimeMillis() - startTime.get();
    boolean success = response.getThrowable() == null;
    String errorMessage = success ? null : response.getThrowable().getMessage();

    monitoringService.recordMetrics(request.getMethodName(), duration, success, errorMessage);

    if (!success) {
      log.error("Method [{}] failed with exception: {}", request.getMethodName(), errorMessage);
    }

    log.info("Method [{}] duration: {}ms", request.getMethodName(), duration);
    if (duration > 1000) {
      log.warn("Method [{}] duration too long: {}ms", request.getMethodName(), duration);
    }
  }

  @Override
  public void afterCompletion(RpcRequest request, RpcResponse response, Throwable ex) {
    startTime.remove();
  }

  public void recordMetrics(String methodName, long executionTime, boolean success, String errorMessage) {
    methodMetricsMap.computeIfAbsent(methodName, k -> Collections.synchronizedList(new ArrayList<>()))
        .add(new MethodMetrics(methodName, executionTime, LocalDateTime.now(), success, errorMessage));
  }
}
