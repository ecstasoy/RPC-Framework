package org.example.rpc.monitor.api;

import org.example.rpc.common.annotations.RpcService;
import org.example.rpc.monitor.model.MethodMetrics;
import org.example.rpc.monitor.model.StatisticalMetrics;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public interface MonitoringService {
    void recordMetrics(String methodName, long executionTime, boolean success, String errorMessage, String metricType);
    
    List<MethodMetrics> getMethodMetrics(String methodName);
    
    Map<String, StatisticalMetrics> getStatistics();
    
    void clearHistoricalData(Duration retention);
}
