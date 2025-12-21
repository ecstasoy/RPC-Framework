package io.ecstasoy.rpc.registry.discovery.api;

import io.ecstasoy.rpc.registry.IRegistryCenter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


/**
 * Interface for service discovery.
 *
 * <p>Service discovery is used to get the service instance list of the specified service name.
 *
 * @see IRegistryCenter
 * @author Kunhua Huang
 */
public interface RpcServiceDiscovery extends IRegistryCenter {

  /**
   * Gets the list of service instances.
   *
   * @param serviceName name of service
   * @return list of service instances
   */
  List<String> getServiceInstaceList(String serviceName) throws ExecutionException;

  /**
   * Gets an instance of the service.
   *
   * @param serviceName name of service
   * @return instance of service
   */
  String getServiceInstance(String serviceName) throws ExecutionException;

  /**
   * Gets the map of service instances.
   *
   * @return map of service instances
   */
  @Deprecated
  Map<String, List<String>> getAllServiceInstance();
}
