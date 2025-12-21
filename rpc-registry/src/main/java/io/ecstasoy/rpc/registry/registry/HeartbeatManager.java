package io.ecstasoy.rpc.registry.registry;

import lombok.extern.slf4j.Slf4j;
import io.ecstasoy.rpc.registry.health.ServiceHealthManager;
import io.ecstasoy.rpc.registry.zookeeper.ZookeeperHelper;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Heartbeat manager.
 *
 * <p>Send heartbeats to the registry server to keep the service instance alive.
 *
 * <p>Record the heartbeat status and update the health status of the service instance.
 *
 * <p>Start and stop the heartbeat task.
 *
 * <p>Shut down the heartbeat executor.
 */
@Slf4j
@Component
public class HeartbeatManager implements DisposableBean {
  private static final int BASE_HEARTBEAT_INTERVAL = 30;
  /*private static final int MAX_RANDOM_DELAY = 10;
  private final Random random = new Random();*/
  private final ScheduledExecutorService heartbeatExecutor;
  private final ServiceHealthManager serviceHealthManager;
  private final ZookeeperHelper zookeeperHelper;
  private final Map<String, ScheduledFuture<?>> heartbeatTasks = new ConcurrentHashMap<>();

  @Autowired
  public HeartbeatManager(ServiceHealthManager serviceHealthManager,
                          ZookeeperHelper zookeeperHelper) {
    this.heartbeatExecutor = Executors.newScheduledThreadPool(
        Runtime.getRuntime().availableProcessors(),
        r -> {
          Thread t = new Thread(r, "heartbeat-worker");
          t.setDaemon(true);
          return t;
        }
    );
    this.serviceHealthManager = serviceHealthManager;
    this.zookeeperHelper = zookeeperHelper;
  }


  private void sendHeartbeat(String serviceName, String instanceId) {
    try {
      zookeeperHelper.updateHealthStatus(serviceName, instanceId);
      serviceHealthManager.recordHeartbeatSuccess(serviceName, instanceId);
      log.debug("Heartbeat success for [{}#{}]", serviceName, instanceId);
    } catch (Exception e) {
      log.error("Heartbeat failed for [{}#{}]", serviceName, instanceId, e);
      serviceHealthManager.recordHeartbeatFailure(serviceName, instanceId, zookeeperHelper);
    }
  }

  public void startHeartbeat(String serviceName, String instanceId) {
    String key = getHeartbeatKey(serviceName, instanceId);
    /*int initialDelay = random.nextInt(MAX_RANDOM_DELAY);*/
    ScheduledFuture<?> future = heartbeatExecutor.scheduleWithFixedDelay(
        () -> sendHeartbeat(serviceName, instanceId),
        0,
        BASE_HEARTBEAT_INTERVAL,
        TimeUnit.SECONDS
    );
    heartbeatTasks.put(key, future);
  }

  private String getHeartbeatKey(String serviceName, String instanceId) {
    return serviceName + "#" + instanceId;
  }

  @Override
  public void destroy() {
    heartbeatTasks.values().forEach(future -> future.cancel(false));
    shutdownExecutor(heartbeatExecutor);
  }

  private void shutdownExecutor(ExecutorService executor) {
    executor.shutdown();
    try {
      if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
