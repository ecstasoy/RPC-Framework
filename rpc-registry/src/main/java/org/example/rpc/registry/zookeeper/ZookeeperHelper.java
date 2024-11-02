package org.example.rpc.registry.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.GetDataBuilder;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

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
public class  ZookeeperHelper implements DisposableBean {

  public static final String ZOOKEEPER_ADDRESS = "127.0.0.1:2181";
  public static final String BASE_RPC_PATH = "/rpc";
  public static final String SERVICE_PATH = "/service";
  public static final String HEALTH_PATH = "/health";
  private static volatile CuratorFramework zookeeperClient;

  private static final Map<String, List<String>> RPC_SERVICE_ADDRESS_MAP =
      new ConcurrentHashMap<>();
  public static final Set<String> PATH_SET = new ConcurrentSkipListSet<>();

  /**
   * Creates service address node in Zookeeper.
   *
   * @param serviceName service name
   */
  public void createServiceInstanceNode(String serviceName) {
    createServiceInstanceNode(serviceName, null);
  }

  /**
   * Creates service address node in Zookeeper.
   *
   * @param serviceName service name
   * @param data service address
   */
  public void createServiceInstanceNode(String serviceName, InetSocketAddress data) {
    checkInit();
    String serviceNodePath = BASE_RPC_PATH + SERVICE_PATH + "/" + serviceName;
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

      byte[] healthData = "UP".getBytes(StandardCharsets.UTF_8);
      zookeeperClient.create()
          .creatingParentsIfNeeded()
          .withMode(CreateMode.EPHEMERAL)
          .forPath(instancePath, healthData);

      log.info("Created service instance: {}", instancePath);
    }
    catch (Exception e) {
      log.error("Create service instance node for [{}] failed.", serviceName, e);
      throw new RuntimeException("Create service instance node for [" + serviceName + "] failed.", e);
    }
  }

  /**
   * Gets list of service nodes.
   *
   * @param serviceName service name
   */
  public List<String> getServiceInstanceNode(String serviceName) {
    checkInit();
    String serviceNodePath = BASE_RPC_PATH + SERVICE_PATH + "/" + serviceName;
    try {
      List<String> instanceNodes = zookeeperClient.getChildren().forPath(serviceNodePath);
      List<String> serviceAddressList = new ArrayList<>();
      for (String instanceNode : instanceNodes) {
        try {
          byte[] bytes = zookeeperClient.getData().forPath(serviceNodePath + "/" + instanceNode);
          String address = new String(bytes, StandardCharsets.UTF_8);
          log.debug("Service address for node {}: {}", instanceNode, address);
          serviceAddressList.add(instanceNode);
        } catch (Exception e) {
          log.error("Get service address for instance [{}] of service [{}] failed.",
              instanceNode, serviceName, e);
        }
      }
      log.debug("Service instances for {}: {}", serviceName, serviceAddressList);
      return serviceAddressList;
    } catch (Exception e) {
      log.error("Get service instances for [{}] failed.", serviceName, e);
      throw new RuntimeException("Get service instances for [" + serviceName + "] failed.", e);
    }
  }

  /**
   * Gets list of all service nodes.
   *
   * @return list of all service nodes
   */
  public Map<String, List<String>> getAllServiceInstanceNode() {
    Map<String, List<String>> result = new HashMap<>();
    try {
      List<String> serviceNodeList = zookeeperClient.getChildren().forPath(BASE_RPC_PATH);
      for (String s : serviceNodeList) {
        result.put(s, getServiceInstanceNode(s));
      }
      return result;
    } catch (Exception e) {
      log.error("Get all service instances failed.", e);
      return result;
    }
  }

  /**
   * Register watcher for service nodes.
   */
  private void checkInit() {
    if (zookeeperClient != null && zookeeperClient.getState() == CuratorFrameworkState.STARTED) {
      return;
    }

    ExponentialBackoffRetry retry = new ExponentialBackoffRetry(1000, 3);
    synchronized (CuratorFramework.class) {
      if (zookeeperClient == null) {
        zookeeperClient = CuratorFrameworkFactory.builder().connectString(ZOOKEEPER_ADDRESS)
            .retryPolicy(retry).build();
      }
      zookeeperClient.start();

      try {
        if (!zookeeperClient.blockUntilConnected(30, TimeUnit.SECONDS)) {
          throw new RuntimeException("Zookeeper connection timeout.");
        }
      } catch (InterruptedException e) {
        log.info("Response interrupted when waiting for connection.");
        throw new RuntimeException(e);
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
    checkInit();
    String path = BASE_RPC_PATH + "/" + serviceName;
    PathChildrenCache pathChildrenCache = new PathChildrenCache(zookeeperClient, path, true);
    PathChildrenCacheListener listener = (client, event) -> {
      final GetDataBuilder data = client.getData();
      if (data != null) {
        switch (event.getType()) {
          case CHILD_ADDED:
            log.debug("Node added: [{}]", event.getData().getPath());
            break;
          case CHILD_REMOVED:
            log.debug("Node removed: [{}]", event.getData().getPath());
            break;
          case CHILD_UPDATED:
            log.debug("Node updated: [{}]", event.getData().getPath());
            break;
          default:
            log.debug("Unknown event: [{}]", event.getType());
        }
      }
      List<String> list = client.getChildren().forPath(path);
    };

    pathChildrenCache.getListenable().addListener(listener);
    pathChildrenCache.start();
  }

  /**
   * Closes the Zookeeper client.
   */
  @Override
  public void destroy() {
    log.info("Closing Zookeeper client.");
    zookeeperClient.close();
  }

  public boolean checkHealthNode(String serviceName, String instanceId) {
    checkInit();
    String instancePath = BASE_RPC_PATH + SERVICE_PATH + "/" + serviceName + "/" + instanceId;
    try {
      byte[] healthData = zookeeperClient.getData().forPath(instancePath);
      return healthData != null && "UP".equals(new String(healthData, StandardCharsets.UTF_8));
    } catch (Exception e) {
      log.error("Check health status failed for {}", instancePath, e);
      return false;
    }
  }

  public void updateHealthStatus(String serviceName, String instanceId) {
    checkInit();
    String instancePath = BASE_RPC_PATH + SERVICE_PATH + "/" + serviceName + "/" + instanceId;
    try {
      byte[] healthData = "UP".getBytes(StandardCharsets.UTF_8);
      zookeeperClient.setData().forPath(instancePath, healthData);
      log.debug("Updated health status for {}", instancePath);
    } catch (Exception e) {
      log.error("Failed to update health status for {}", instancePath, e);
      throw new RuntimeException("Failed to update health status", e);
    }
  }

  public void removeServiceInstanceNode(String serviceName, String instanceId) {
    checkInit();
    final String serviceNode = BASE_RPC_PATH + "/" + serviceName + "/" + instanceId;
    try {
      zookeeperClient.delete().forPath(serviceNode);
    } catch (Exception e) {
      log.error("Remove service instance node for [{}] failed.", serviceName, e);
      throw new RuntimeException("Remove service instance node for [" + serviceName + "] failed.", e);
    }
  }

  public void removeHealthNode(String serviceName, String instanceId) {
    checkInit();
    final String healthNode = BASE_RPC_PATH + "/" + serviceName + "/" + instanceId + "/health";
    try {
      zookeeperClient.delete().forPath(healthNode);
    } catch (Exception e) {
      log.error("Remove health node for [{}] failed.", serviceName, e);
      throw new RuntimeException("Remove health node for [" + serviceName + "] failed.", e);
    }
  }

}