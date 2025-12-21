package io.ecstasoy.rpc.monitor.controller;

import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import io.ecstasoy.rpc.monitor.api.MonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

import com.google.gson.Gson;

/**
 * Server monitor controller.
 */
@Slf4j
@RestController
@RequestMapping("/monitor/server")
public class ServerMonitorController {

  private final MonitoringService monitoringService;
  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  /**
   * Constructor.
   *
   * @param monitoringService monitoring service
   */
  @Autowired
  public ServerMonitorController(
      @Qualifier("defaultMonitoringService") MonitoringService monitoringService) {
    this.monitoringService = monitoringService;
  }

  /**
   * Get method metrics.
   *
   * @param methodName method name
   * @return method metrics
   */
  @GetMapping("/metrics/{methodName}")
  public String getMethodMetrics(@PathVariable String methodName) {
    return gson.toJson(monitoringService.getMethodMetrics(methodName));
  }

  /**
   * Get all method metrics.
   *
   * @return all method metrics
   */
  @GetMapping("/statistics")
  public String getStatistics() {
    return gson.toJson(monitoringService.getStatistics());
  }

  /**
   * Clear historical data.
   *
   * @param retentionDays retention days
   */
  @DeleteMapping("/clear")
  public void clearHistoricalData(@RequestParam(defaultValue = "7") int retentionDays) {
    monitoringService.clearHistoricalData(Duration.ofDays(retentionDays));
  }
} 