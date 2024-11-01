package org.example.rpc.registry.discovery.api;

import org.example.rpc.registry.IRegistryCenter;

import java.util.List;
import java.util.Map;


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
   * Gets the type of registry center.
   *
   * @param serviceName name of service
   * @return type of registry center
   */
  List<String> getServiceInstaceList(String serviceName);

  /**
   * Gets an instance of the service.
   *
   * @param serviceName name of service
   * @return instance of service
   */
  String getServiceInstance(String serviceName);

  /**
   * Gets the map of service instances.
   *
   * @return map of service instances
   */
  @Deprecated
  Map<String, List<String>> getAllServiceInstance();
}
