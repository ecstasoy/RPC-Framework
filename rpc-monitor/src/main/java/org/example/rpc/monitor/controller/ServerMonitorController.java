package org.example.rpc.monitor.controller;

import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.example.rpc.monitor.api.MonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

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