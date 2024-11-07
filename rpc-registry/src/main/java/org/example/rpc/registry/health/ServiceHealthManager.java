package org.example.rpc.registry.health;

import lombok.extern.slf4j.Slf4j;
import org.example.rpc.registry.zookeeper.ZookeeperHelper;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ServiceHealthManager {
  private static final int MAX_FAILURE_COUNT = 3;
  private static final long FAILURE_WINDOW = 60000;
  private final Map<String, Integer> healthFailureCount = new ConcurrentHashMap<>();
  private final Map<String, Long> lastFailureTime = new ConcurrentHashMap<>();

  public void recordHeartbeatFailure(String serviceName, String instanceId, ZookeeperHelper zookeeperHelper) {
    String key = getInstanceKey(serviceName, instanceId);
    healthFailureCount.compute(key, (k, v) -> v == null ? 1 : v + 1);
    lastFailureTime.put(key, System.currentTimeMillis());
    checkAndRemoveInstance(serviceName, instanceId, zookeeperHelper);
  }

  public void recordHeartbeatSuccess(String serviceName, String instanceId) {
    String key = getInstanceKey(serviceName, instanceId);
    healthFailureCount.remove(key);
    lastFailureTime.remove(key);
  }

  private void checkAndRemoveInstance(String serviceName, String instanceId, ZookeeperHelper zookeeperHelper) {
    String key = getInstanceKey(serviceName, instanceId);
    Integer failures = healthFailureCount.get(key);
    Long lastFailure = lastFailureTime.get(key);

    if (failures != null && lastFailure != null) {
      long now = System.currentTimeMillis();
      if (failures >= MAX_FAILURE_COUNT && (now - lastFailure) <= FAILURE_WINDOW) {
        try {
          log.warn("Service instance [{}#{}] is unhealthy, removing it", serviceName, instanceId);
          zookeeperHelper.removeServiceInstanceNode(serviceName, instanceId);
          healthFailureCount.remove(key);
          lastFailureTime.remove(key);
        } catch (Exception e) {
          log.error("Failed to remove unhealthy service instance [{}#{}]", serviceName, instanceId, e);
        }
      }
    }
  }

  private String getInstanceKey(String serviceName, String instanceId) {
    return serviceName + "#" + instanceId;
  }
} 