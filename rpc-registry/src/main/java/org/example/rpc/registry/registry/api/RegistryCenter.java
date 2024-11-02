package org.example.rpc.registry.registry.api;

import org.example.rpc.common.enums.RegistryCenterType;
import org.example.rpc.registry.registry.api.model.ServiceInstance;

import java.util.List;
import java.util.Map;

public interface RegistryCenter {
  void register(ServiceInstance instance);
  void unregister(ServiceInstance instance);
  List<ServiceInstance> getInstances(String serviceName);
  void sendHeartbeat();
  Map<String, List<ServiceInstance>> getAllInstances();
  RegistryCenterType getRegistryCenterType();
}