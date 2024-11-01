package org.example.rpc.interceptor.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.rpc.common.circuit.CircuitBreakerState;
import org.example.rpc.interceptor.RpcInterceptor;
import org.example.rpc.protocol.model.RpcRequest;
import org.example.rpc.protocol.model.RpcResponse;
import org.example.rpc.monitor.api.MonitoringService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client circuit breaker interceptor.
 *
 * <p>Record metrics for client-side circuit breaker.
 * <p>Record the duration of the request, whether the request is successful, the error message, and the state of the circuit breaker.
 * <p>Remove the start time of the request after the request is completed.
 *
 * @author Kunhua Huang
 * @see RpcInterceptor
 * @see MonitoringService
 */
@Slf4j
@Component
public class ClientCircuitBreakerInterceptor implements RpcInterceptor {
  private final MonitoringService clientMonitoringService;
  private final ConcurrentHashMap<String, Long> requestStartTimes = new ConcurrentHashMap<>();

  /**
   * Instantiates a new Client circuit breaker interceptor.
   *
   * @param clientMonitoringService the client monitoring service
   */
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
          request.getClassName() + "#" + request.getMethodName(),
          duration,
          success,
          errorMessage,
          stateMessage
      );
      if (!success) {
        log.error("Method [{}] failed with exception: {}", request.getMethodName(), errorMessage);
      }

      NumberFormat formatter = new DecimalFormat("#0.0000");
      log.debug("Method [{}] duration: {}ms", request.getMethodName(), formatter.format(duration));
      if (duration > 1000) {
        log.warn("Method [{}] duration too long: {}ms", request.getMethodName(), duration);
      }

      requestStartTimes.remove(request.getSequence());
    }
  }

  @Override
  public void afterCompletion(RpcRequest request, RpcResponse response, Throwable ex) {
    requestStartTimes.remove(request.getSequence());
  }
}