package io.ecstasoy.rpc.monitor.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import io.ecstasoy.rpc.common.circuit.CircuitBreakerState;
import io.ecstasoy.rpc.monitor.api.MonitoringService;
import io.ecstasoy.rpc.monitor.impl.ClientMonitoringServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

/**
 * Client monitor controller.
 *
 * <p>Provide RESTful APIs for client-side monitoring.
 * <p>Provide APIs for querying service metrics, statistics, circuit breaker state, and clearing historical data.
 *
 * @author Kunhua Huang
 * @see MonitoringService
 */
@Slf4j
@RestController
@RequestMapping("/monitor/client")
public class ClientMonitorController {

  private final MonitoringService clientMonitoringService;
  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  /**
   * Instantiates a new Client monitor controller.
   *
   * @param clientMonitoringService the client monitoring service
   */
  @Autowired
  public ClientMonitorController(
      @Qualifier("clientMonitoringService") MonitoringService clientMonitoringService) {
    this.clientMonitoringService = clientMonitoringService;
  }

  /**
   * Gets service metrics.
   *
   * @param serviceName the service name
   * @return the service metrics
   */
  @GetMapping("/metrics/{serviceName}")
  public String getServiceMetrics(@PathVariable String serviceName) {
    return gson.toJson(clientMonitoringService.getStatistics().get(serviceName));
  }

  @GetMapping("/statistics")
  public String getStatistics() {
    return gson.toJson(clientMonitoringService.getStatistics());
  }

  /**
   * Gets circuit breaker state.
   *
   * @param serviceName the service name
   * @return the circuit breaker state
   */
  @GetMapping("/circuit-breaker/state/{serviceName}")
  public CircuitBreakerState getCircuitBreakerState(@PathVariable String serviceName) {
    return ((ClientMonitoringServiceImpl) clientMonitoringService).getCurrentState(serviceName);
  }

  /**
   * Clear historical data.
   *
   * @param retentionDays the retention days
   */
  @DeleteMapping("/metrics/clear")
  public void clearHistoricalData(@RequestParam(defaultValue = "7") int retentionDays) {
    clientMonitoringService.clearHistoricalData(Duration.ofDays(retentionDays));
  }
} 