package org.example.rpc.server.controller;

import org.example.rpc.core.monitor.api.MonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.Duration;

@RestController
@RequestMapping("/monitor")
public class MonitoringController {

  private final MonitoringService monitoringService;

  @Autowired
  public MonitoringController(MonitoringService monitoringService) {
    this.monitoringService = monitoringService;
  }

  @GetMapping("/metrics/{methodName}")
  public String getMethodMetrics(@PathVariable String methodName) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    String json = gson.toJson(monitoringService.getMethodMetrics(methodName));
    return json;
  }

  @GetMapping("/statistics")
  public String getStatistics() {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    String json = gson.toJson(monitoringService.getStatistics());
    return json;
  }

  @DeleteMapping("/clear")
  public void clearHistoricalData(@RequestParam Duration retention) {
    monitoringService.clearHistoricalData(retention);
  }
}