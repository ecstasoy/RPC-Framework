package org.example.rpc.core.test.interceptor;

import org.example.rpc.core.common.circuit.CircuitBreakerState;
import org.example.rpc.core.common.enums.MetricType;
import org.example.rpc.core.interceptor.impl.PerformanceInterceptor;
import org.example.rpc.core.model.RpcRequest;
import org.example.rpc.core.model.RpcResponse;
import org.example.rpc.core.monitor.api.MonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

class PerformanceInterceptorTest {

  private PerformanceInterceptor performanceInterceptor;
  private MonitoringService monitoringService;

  @BeforeEach
  void setUp() {
    monitoringService = Mockito.mock(MonitoringService.class);
    performanceInterceptor = new PerformanceInterceptor(monitoringService);
  }

  @Test
  void testPreHandle() {
    RpcRequest request = new RpcRequest();
    request.setMethodName("testMethod");
    performanceInterceptor.preHandle(request);
    // 这里可以验证是否正确设置了开始时间
  }

  @Test
  void testPostHandle() {
    RpcRequest request = new RpcRequest();
    request.setMethodName("testMethod");
    RpcResponse response = new RpcResponse();
    response.setThrowable(null);

    performanceInterceptor.preHandle(request);
    performanceInterceptor.postHandle(request, response, null);

    verify(monitoringService).recordMetrics(eq("null#testMethod"), anyLong(), eq(true), isNull(), eq(MetricType.NORMAL_REQUEST.toString()));
  }

  @Test
  void testPostHandleWithException() {
    RpcRequest request = new RpcRequest();
    request.setMethodName("testMethod");
    RpcResponse response = new RpcResponse();
    response.setThrowable(new RuntimeException("Test Exception"));

    performanceInterceptor.preHandle(request);
    performanceInterceptor.postHandle(request, response, null);

    verify(monitoringService).recordMetrics(eq("null#testMethod"), anyLong(), eq(false), eq("Test Exception"), eq(MetricType.EXCEPTION.toString()));
  }
}