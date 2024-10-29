package org.example.rpc.core.monitor.controller;

import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.example.rpc.core.monitor.api.MonitoringService;
import org.example.rpc.core.monitor.model.MethodMetrics;
import org.example.rpc.core.monitor.model.StatisticalMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

@Slf4j
@RestController
@RequestMapping("/monitor/server")
public class ServerMonitorController {

  private final MonitoringService monitoringService;
  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  @Autowired
  public ServerMonitorController(
      @Qualifier("defaultMonitoringService") MonitoringService monitoringService) {
    this.monitoringService = monitoringService;
  }

  @GetMapping("/metrics/{methodName}")
  public String getMethodMetrics(@PathVariable String methodName) {
    return gson.toJson(monitoringService.getMethodMetrics(methodName));
  }

  @GetMapping("/statistics")
  public String getStatistics() {
    return gson.toJson(monitoringService.getStatistics());
  }

  @DeleteMapping("/clear")
  public void clearHistoricalData(@RequestParam(defaultValue = "7") int retentionDays) {
    monitoringService.clearHistoricalData(Duration.ofDays(retentionDays));
  }
} 