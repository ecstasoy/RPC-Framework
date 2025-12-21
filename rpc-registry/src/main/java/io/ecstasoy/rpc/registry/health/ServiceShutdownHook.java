package io.ecstasoy.rpc.registry.health;

import lombok.extern.slf4j.Slf4j;
import io.ecstasoy.rpc.registry.registry.api.RpcServiceRegistry;
import io.ecstasoy.rpc.registry.registry.param.RpcServiceUnregistryParam;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service shutdown hook.
 *
 * <p>It is used to unregister services when the application is shutting down.
 *
 * @see RpcServiceRegistry
 * @author Kunhua Huang
 */
@Slf4j
@Component
public class ServiceShutdownHook {

  private final RpcServiceRegistry serviceRegistry;
  private final Map<String, RpcServiceUnregistryParam> registeredServices;

  /**
   * Constructor.
   *
   * @param serviceRegistry service registry
   */
  public ServiceShutdownHook(RpcServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
    this.registeredServices = new ConcurrentHashMap<>();
    registerShutdownHook();
  }

  /**
   * Add registered service.
   *
   * @param instanceId instance ID
   * @param param service unregistry param
   */
  public void addRegisteredService(String instanceId, RpcServiceUnregistryParam param) {
    registeredServices.put(instanceId, param);
  }

  /**
   * The shutdown hook.
   */
  private void registerShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      log.info("Executing shutdown hook - unregistering services");
      registeredServices.values().forEach(param -> {
        try {
          serviceRegistry.unRegister(param);
        } catch (Exception e) {
          log.error("Failed to unregister service [{}]", param.getInstanceId(), e);
        }
      });
    }));
  }


}
