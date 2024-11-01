package org.example.rpc.monitor.controller;

import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.example.rpc.common.circuit.CircuitBreakerState;
import org.example.rpc.monitor.api.MonitoringService;
import org.example.rpc.monitor.impl.ClientMonitoringServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

import com.google.gson.Gson;

@Slf4j
@RestController
@RequestMapping("/monitor/client")
public class ClientMonitorController {

  private final MonitoringService clientMonitoringService;
  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  @Autowired
  public ClientMonitorController(
      @Qualifier("clientMonitoringService") MonitoringService clientMonitoringService) {
    this.clientMonitoringService = clientMonitoringService;
  }

  @GetMapping("/metrics/{serviceName}")
  public String getServiceMetrics(@PathVariable String serviceName) {
    return gson.toJson(clientMonitoringService.getStatistics().get(serviceName));
  }

  @GetMapping("/statistics")
  public String getStatistics() {
    return gson.toJson(clientMonitoringService.getStatistics());
  }

  @GetMapping("/circuit-breaker/state/{serviceName}")
  public CircuitBreakerState getCircuitBreakerState(@PathVariable String serviceName) {
    return ((ClientMonitoringServiceImpl) clientMonitoringService).getCurrentState(serviceName);
  }

  @DeleteMapping("/metrics/clear")
  public void clearHistoricalData(@RequestParam(defaultValue = "7") int retentionDays) {
    clientMonitoringService.clearHistoricalData(Duration.ofDays(retentionDays));
  }
} 