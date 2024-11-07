package org.example.rpc.registry.zookeeper;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "rpc.zookeeper")
@Component
@Data
public class ZookeeperProperties {

  private String zookeeperAddress = "localhost:2181";
  private int sessionTimeout = 30000;
  private int connectionTimeout = 15000;
  private String basePath = "/rpc";
  private String servicePath = "/service";
}
