package org.example.rpc.core.monitor.api;

import org.example.rpc.core.common.annotations.RpcService;
import org.example.rpc.core.monitor.model.MethodMetrics;
import org.example.rpc.core.monitor.model.StatisticalMetrics;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public interface MonitoringService {
    void recordMetrics(String methodName, long executionTime, boolean success, String errorMessage, String metricType);
    
    List<MethodMetrics> getMethodMetrics(String methodName);
    
    Map<String, StatisticalMetrics> getStatistics();
    
    void clearHistoricalData(Duration retention);
}
