package org.example.rpc.registry.registry.api.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.example.rpc.common.enums.RegistryCenterType;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceInstance {
  private String serviceName;
  private String instanceId;
  private String ip;
  private int port;
  private long lastHeartbeat;
  private RegistryCenterType registryCenterType;
  private Map<String, String> metadata;
}