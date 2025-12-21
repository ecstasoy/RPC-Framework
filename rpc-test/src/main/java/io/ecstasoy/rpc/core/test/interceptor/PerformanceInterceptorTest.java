package io.ecstasoy.rpc.core.test.interceptor;

import io.ecstasoy.rpc.common.enums.MetricType;
import io.ecstasoy.rpc.interceptor.impl.PerformanceInterceptor;
import io.ecstasoy.rpc.monitor.api.MonitoringService;
import io.ecstasoy.rpc.protocol.model.RpcRequest;
import io.ecstasoy.rpc.protocol.model.RpcResponse;
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