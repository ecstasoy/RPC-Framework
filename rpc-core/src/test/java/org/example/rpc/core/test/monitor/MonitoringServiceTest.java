package org.example.rpc.core.test.monitor;

import org.example.rpc.core.monitor.impl.DefaultMonitoringServiceImpl;
import org.example.rpc.core.monitor.model.MethodMetrics;
import org.example.rpc.core.monitor.model.StatisticalMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MonitoringServiceTest {

  private DefaultMonitoringServiceImpl monitoringService;

  @BeforeEach
  void setUp() {
    monitoringService = new DefaultMonitoringServiceImpl();
  }

  @Test
  void testRecordMetrics() {
    monitoringService.recordMetrics("testMethod", 200, true, null, null);
    List<MethodMetrics> metrics = monitoringService.getMethodMetrics("testMethod");
    assertEquals(1, metrics.size());
    assertEquals("testMethod", metrics.get(0).getMethodName());
    assertTrue(metrics.get(0).isSuccess());
  }

  @Test
  void testGetStatistics() {
    monitoringService.recordMetrics("testMethod", 200, true, null, null);
    monitoringService.recordMetrics("testMethod", 300, false, "error", null);
    Map<String, StatisticalMetrics> stats = monitoringService.getStatistics();
    assertEquals(1, stats.size());
    assertEquals(250.0, stats.get("testMethod").getAverageTime());
    assertEquals(300, stats.get("testMethod").getMaxTime());
    assertEquals(1, stats.get("testMethod").getSuccessCount());
    assertEquals(2, stats.get("testMethod").getTotalCount());
    assertEquals(1, stats.get("testMethod").getErrorMessages().size());
    assertEquals("error", stats.get("testMethod").getErrorMessages().get(0));
  }

  @Test
  void testClearHistoricalData() {
    monitoringService.recordMetrics("testMethod", 200, true, null, null);
    monitoringService.clearHistoricalData(Duration.ofMinutes(1));
    List<MethodMetrics> metrics = monitoringService.getMethodMetrics("testMethod");
    assertEquals(1, metrics.size());
    monitoringService.clearHistoricalData(Duration.ofSeconds(0));
    metrics = monitoringService.getMethodMetrics("testMethod");
    assertTrue(metrics.isEmpty());
  }
}
