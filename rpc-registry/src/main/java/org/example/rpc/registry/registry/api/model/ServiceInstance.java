package org.example.rpc.registry.registry.api.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.example.rpc.common.enums.RegistryCenterType;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * <p>Service instance.
 *
 * <p>It contains the service name, instance ID, IP, port, last heartbeat time, registry center type, and metadata.
 *
 * @see RegistryCenterType
 * @author Kunhua Huang
 */
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