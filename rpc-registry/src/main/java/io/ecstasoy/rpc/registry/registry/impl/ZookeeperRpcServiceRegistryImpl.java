package io.ecstasoy.rpc.registry.registry.impl;

import lombok.extern.slf4j.Slf4j;
import io.ecstasoy.rpc.common.enums.RegistryCenterType;
import io.ecstasoy.rpc.registry.health.ServiceHealthManager;
import io.ecstasoy.rpc.registry.health.ServiceShutdownHook;
import io.ecstasoy.rpc.registry.registry.HeartbeatManager;
import io.ecstasoy.rpc.registry.registry.param.RpcServiceRegistryParam;
import io.ecstasoy.rpc.registry.registry.param.RpcServiceUnregistryParam;
import io.ecstasoy.rpc.registry.zookeeper.ZookeeperHelper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zookeeper implementation of service registry.
 *
 * <p>Register service to zookeeper.
 * Implement the {@link AbstractRpcServiceRegistry} abstract class.
 *
 * @see AbstractRpcServiceRegistry
 * @author Kunhua Huang
 */
@Primary
@Component
@Slf4j
public class ZookeeperRpcServiceRegistryImpl extends AbstractRpcServiceRegistry {

  private final HeartbeatManager heartbeatManager;
  private final ZookeeperHelper zookeeperHelper;
  private final Map<String, RpcServiceRegistryParam> serviceRegistryParamMap = new ConcurrentHashMap<>();
  private final ServiceHealthManager serviceHealthManager;
  private final ServiceShutdownHook serviceShutdownHook = new ServiceShutdownHook(this);

  /**
   * Constructor.
   *
   * @param zookeeperHelper zookeeper helper
   */
  public ZookeeperRpcServiceRegistryImpl(HeartbeatManager heartbeatManager, ZookeeperHelper zookeeperHelper, ServiceHealthManager serviceHealthManager) {
    this.heartbeatManager = heartbeatManager;
    this.zookeeperHelper = zookeeperHelper;
    this.serviceHealthManager = serviceHealthManager;
  }

  @Override
  public RegistryCenterType getRegistryCenterType() {
    return RegistryCenterType.ZOOKEEPER;
  }

  @Override
  void doRegister(RpcServiceRegistryParam registryParam) {
    InetSocketAddress address = new InetSocketAddress(registryParam.getIp(),
        registryParam.getPort());
    zookeeperHelper.createServiceInstanceNode(registryParam.getServiceName(), address);
    serviceRegistryParamMap.put(registryParam.getInstanceId(), registryParam);

    RpcServiceUnregistryParam unregistryParam = RpcServiceUnregistryParam.builder()
        .ip(registryParam.getIp())
        .port(registryParam.getPort())
        .serviceName(registryParam.getServiceName())
        .instanceId(registryParam.getInstanceId())
        .build();

    serviceShutdownHook.addRegisteredService(registryParam.getInstanceId(), unregistryParam);
    heartbeatManager.startHeartbeat(registryParam.getServiceName(), registryParam.getInstanceId());
  }

  @Override
  public void doUnregister(RpcServiceUnregistryParam unregistryParam) {
    String instanceId = unregistryParam.getInstanceId();
    RpcServiceRegistryParam param = serviceRegistryParamMap.get(instanceId);
    if (param != null) {
      try {
        zookeeperHelper.removeServiceInstanceNode(param.getServiceName(), instanceId);
        serviceRegistryParamMap.remove(instanceId);
        log.info("Service instance [{}] unregistered successfully", instanceId);
      } catch (Exception e) {
        log.error("Failed to unregister service instance [{}]", instanceId, e);
        throw new RuntimeException("Failed to unregister service", e);
      }
    }
  }
}
