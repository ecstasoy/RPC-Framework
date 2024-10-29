package org.example.rpc.core.interceptor.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.rpc.core.common.circuit.CircuitBreakerState;
import org.example.rpc.core.interceptor.RpcInterceptor;
import org.example.rpc.core.model.RpcRequest;
import org.example.rpc.core.model.RpcResponse;
import org.example.rpc.core.monitor.api.MonitoringService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ClientCircuitBreakerInterceptor implements RpcInterceptor {
  private final MonitoringService clientMonitoringService;
  private final ConcurrentHashMap<String, Long> requestStartTimes = new ConcurrentHashMap<>();

  public ClientCircuitBreakerInterceptor(@Qualifier("clientMonitoringService") MonitoringService clientMonitoringService) {
    this.clientMonitoringService = clientMonitoringService;
  }

  @Override
  public boolean preHandle(RpcRequest request) {
    requestStartTimes.put(request.getSequence(), System.currentTimeMillis());
    return true;
  }

  @Override
  public void postHandle(RpcRequest request, RpcResponse response, CircuitBreakerState state) {
    Long startTime = requestStartTimes.get(request.getSequence());
    if (startTime != null) {
      long duration = System.currentTimeMillis() - startTime;
      boolean success = response.getThrowable() == null;
      String errorMessage = success ? null : response.getThrowable().getMessage();
      String stateMessage = state == null ? null : state.toString();

      clientMonitoringService.recordMetrics(
          request.getClassName(),
          duration,
          success,
          errorMessage,
          stateMessage
      );
      requestStartTimes.remove(request.getSequence());
    }
  }

  @Override
  public void afterCompletion(RpcRequest request, RpcResponse response, Throwable ex) {
    requestStartTimes.remove(request.getSequence());
  }
}