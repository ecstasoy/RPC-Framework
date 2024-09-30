package org.example.rpc.core.ext.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLBackgroundPathAndBytesable;
import org.apache.curator.framework.api.GetDataBuilder;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

/**
 * Helper class for Zookeeper.
 */
@Slf4j
@Component
public class ZookeeperHelper implements DisposableBean {

  public static final String ZOOKEEPER_ADDRESS = "127.0.0.1:2181";
  public static final String BASE_RPC_PATH = "/rpc";
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
    final String serviceNode = BASE_RPC_PATH + "/" + serviceName + "/node";
    try {
      if (data == null) {
        zookeeperClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
            .forPath(serviceNode);
      } else {
        zookeeperClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
            .forPath(serviceNode, data.toString().getBytes(StandardCharsets.UTF_8));
      }
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
    final String serviceNodeName = BASE_RPC_PATH + "/" + serviceName;
    try {
      List<String> serviceNodeList = zookeeperClient.getChildren().forPath(serviceNodeName);
      List<String> serviceAddressList = new ArrayList<>();
      for (String childNode : serviceNodeList) {
        try {
          final byte[] bytes = zookeeperClient.getData().forPath(serviceNodeName + "/" + childNode);
          serviceAddressList.add(new String(bytes, StandardCharsets.UTF_8));
        } catch (Exception e) {
          log.error("Get service address for [{}] failed.", serviceName, e);
        }
      }
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
  public void destroy() throws Exception {
    log.info("Closing Zookeeper client.");
    zookeeperClient.close();
  }
}