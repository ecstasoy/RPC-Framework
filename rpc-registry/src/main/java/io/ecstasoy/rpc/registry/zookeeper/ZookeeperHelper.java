package io.ecstasoy.rpc.registry.zookeeper;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import io.ecstasoy.rpc.common.exception.RpcException;
import io.ecstasoy.rpc.registry.health.ServiceHealthManager;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Helper class for Zookeeper.
 *
 * <p>It provides methods to create service address node, get service address node, get all service
 * address nodes, and register watcher for service nodes.
 * It also provides a method to close the Zookeeper client.
 *
 * @author Kunhua Huang
 */
@Slf4j
@Component
public class ZookeeperHelper implements DisposableBean {

  private final ZookeeperProperties zookeeperProperties;
  private final ServiceHealthManager serviceHealthManager;
  private final Cache<String, List<String>> serviceInstanceCache;
  private volatile CuratorFramework zookeeperClient;

  /**
   * Constructor.
   *
   * @param zookeeperProperties  Zookeeper properties
   * @param serviceHealthManager service health manager
   */
  public ZookeeperHelper(ZookeeperProperties zookeeperProperties, ServiceHealthManager serviceHealthManager) {
    this.zookeeperProperties = zookeeperProperties;
    this.serviceHealthManager = serviceHealthManager;
    this.serviceInstanceCache = CacheBuilder.newBuilder()
        .expireAfterWrite(30, TimeUnit.SECONDS)
        .maximumSize(1000)
        .recordStats()
        .build(new CacheLoader<String, List<String>>() {
          @Override
          public List<String> load(String serviceName) throws Exception {
            return loadServiceInstanceFromZookeeper(serviceName);
          }

          @Override
          public ListenableFuture<List<String>> reload(String serviceName, List<String> oldValue) {
            return ListenableFutureTask.create(() -> loadServiceInstanceFromZookeeper(serviceName));
          }
        });
  }

  private List<String> loadServiceInstanceFromZookeeper(String serviceName) {
    String serviceNodePath = zookeeperProperties.getBasePath() + zookeeperProperties.getServicePath() + "/" + serviceName;
    try {
      return doGetServiceInstances(serviceNodePath);
    } catch (Exception e) {
      throw new RpcException("INTERNAL_ERROR", "Failed to get service instances", 500);
    }
  }


  /**
   * Creates service address node in Zookeeper.
   *
   * @param serviceName service name
   * @param data        service address
   */
  public void createServiceInstanceNode(String serviceName, InetSocketAddress data) {
    checkInit();
    String serviceNodePath = zookeeperProperties.getBasePath() + zookeeperProperties.getServicePath() + "/" + serviceName;
    String addressData = data.getHostString() + ":" + data.getPort();
    String instanceId = serviceName + "#" + addressData;
    String instancePath = serviceNodePath + "/" + instanceId;
    try {
      if (zookeeperClient.checkExists().forPath(serviceNodePath) == null) {
        zookeeperClient.create()
            .creatingParentsIfNeeded()
            .withMode(CreateMode.PERSISTENT)
            .forPath(serviceNodePath);
      }

      if (zookeeperClient.checkExists().forPath(instancePath) != null) {
        // 如果存在，先删除旧节点
        zookeeperClient.delete().forPath(instancePath);
        log.info("Deleted existing service instance: {}", instancePath);
      }

      byte[] healthData = "UP".getBytes(StandardCharsets.UTF_8);
      zookeeperClient.create()
          .creatingParentsIfNeeded()
          .withMode(CreateMode.EPHEMERAL)
          .forPath(instancePath, healthData);

      registerWatcher(serviceName);

      log.info("Created service instance: {}", instancePath);
    } catch (Exception e) {
      log.error("Create service instance node for [{}] failed.", serviceName, e);
      throw new RuntimeException("Create service instance node for [" + serviceName + "] failed.", e);
    }
  }

  /**
   * Gets service address node.
   *
   * @param serviceName service name
   * @return list of service address nodes
   */
  public List<String> getServiceInstanceNode(String serviceName) throws ExecutionException {
    return serviceInstanceCache.get(serviceName, () -> {
      String serviceNodePath = zookeeperProperties.getBasePath() + zookeeperProperties.getServicePath() + "/" + serviceName;
      try {
        return doGetServiceInstances(serviceNodePath);
      } catch (Exception e) {
        throw new RpcException("INTERNAL_ERROR", "Failed to get service instances", 500);
      }
    });
  }

  private List<String> doGetServiceInstances(String serviceNodePath) throws Exception {
    checkInit();
    List<String> instanceNodes = zookeeperClient.getChildren().forPath(serviceNodePath);
    return instanceNodes.stream()
        .map(node -> {
          log.debug("Found service instance node: {}", node);
          return node;
        })
        .collect(Collectors.toList());
  }

  /**
   * Gets all service nodes.
   *
   * @return map of service name and list of service nodes
   */
  public Map<String, List<String>> getAllServiceInstanceNode() {
    checkInit();
    Map<String, List<String>> result = new HashMap<>();
    try {
      List<String> serviceNodeList = zookeeperClient.getChildren().forPath(zookeeperProperties.getBasePath());

      for (String serviceName : serviceNodeList) {
        String serviceNodePath = zookeeperProperties.getBasePath() + zookeeperProperties.getServicePath() + "/" + serviceName;
        List<String> instances = serviceInstanceCache.get(serviceName, () -> doGetServiceInstances(serviceNodePath));
        result.put(serviceName, instances);
      }
    } catch (Exception e) {
      log.error("Get all service instances failed.", e);
    }
    return result;
  }

  /**
   * Register watcher for service nodes.
   */
  private void checkInit() {
    if (zookeeperClient != null && zookeeperClient.getState() == CuratorFrameworkState.STARTED) {
      return;
    }

    ExponentialBackoffRetry retry = new ExponentialBackoffRetry(1000, 3);
    synchronized (ZookeeperHelper.class) {
      if (zookeeperClient == null) {
        zookeeperClient = CuratorFrameworkFactory.builder()
            .connectString(zookeeperProperties.getZookeeperAddress())
            .sessionTimeoutMs(zookeeperProperties.getSessionTimeout())
            .connectionTimeoutMs(zookeeperProperties.getConnectionTimeout())
            .retryPolicy(retry)
            .build();
      }
      zookeeperClient.start();

      try {
        if (!zookeeperClient.blockUntilConnected(zookeeperProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS)) {
          throw new RuntimeException("Zookeeper connection timeout");
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("Interrupted while connecting to Zookeeper", e);
      }
    }
  }

  /**
   * Returns the Zookeeper client.
   *
   * @return Zookeeper client
   */
  public CuratorFramework getZookeeperClient() {
    checkInit();
    return zookeeperClient;
  }

  private void registerWatcher(String serviceName) throws Exception {
    String servicePath = zookeeperProperties.getBasePath() + zookeeperProperties.getServicePath() + "/" + serviceName;
    PathChildrenCache watcher = new PathChildrenCache(zookeeperClient, servicePath, true);
    log.debug("Register watcher for service: {}", serviceName);
    watcher.getListenable().addListener((client, event) -> {
      if (Objects.requireNonNull(event.getType()) == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
        String instanceId = event.getData().getPath().substring(servicePath.length() + 1);
        serviceHealthManager.recordHeartbeatFailure(serviceName, instanceId, this);
      }
    });
    watcher.start();
  }

  /**
   * Closes the Zookeeper client.
   */
  @Override
  public void destroy() {
    log.info("Closing Zookeeper client.");
    zookeeperClient.close();
  }

  /**
   * Check health status of a service node.
   *
   * @param serviceName service name
   * @param instanceId  instance id
   * @return true if health status is UP, false otherwise
   */
  public boolean checkHealthNode(String serviceName, String instanceId) {
    checkInit();
    String instancePath = zookeeperProperties.getBasePath() + zookeeperProperties.getServicePath() + "/" + serviceName + "/" + instanceId;
    try {
      byte[] healthData = zookeeperClient.getData().forPath(instancePath);
      return healthData != null && "UP".equals(new String(healthData, StandardCharsets.UTF_8));
    } catch (Exception e) {
      log.error("Check health status failed for {}", instancePath, e);
      return false;
    }
  }

  /**
   * Update health status of a service node.
   *
   * @param serviceName service name
   * @param instanceId  instance id
   */
  public void updateHealthStatus(String serviceName, String instanceId) {
    checkInit();
    String instancePath = zookeeperProperties.getBasePath() + zookeeperProperties.getServicePath() + "/" + serviceName + "/" + instanceId;
    CompletableFuture.runAsync(() -> {
      try {
        byte[] healthData = "UP".getBytes(StandardCharsets.UTF_8);
        zookeeperClient.setData().forPath(instancePath, healthData);
        log.debug("Updated health status for {}", instancePath);
      } catch (Exception e) {
        log.error("Failed to update health status for {}", instancePath, e);
      }
    });
  }


  /**
   * Remove service instance node.
   *
   * @param serviceName service name
   * @param instanceId  instance id
   */
  public void removeServiceInstanceNode(String serviceName, String instanceId) {
    checkInit();
    String instancePath = zookeeperProperties.getBasePath() + zookeeperProperties.getServicePath() + "/" + serviceName + "/" + instanceId;
    try {
      zookeeperClient.delete().forPath(instancePath);
    } catch (Exception e) {
      log.error("Remove service instance node for [{}] failed.", serviceName, e);
      throw new RuntimeException("Remove service instance node for [" + serviceName + "] failed.", e);
    }
  }
}