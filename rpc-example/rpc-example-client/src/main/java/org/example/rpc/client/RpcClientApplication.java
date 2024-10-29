package org.example.rpc.client;

import org.example.rpc.core.common.annotations.RpcServiceScan;
import org.example.rpc.core.transport.client.NettyClient;
import org.example.rpc.core.protocol.serialize.SerializerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.CountDownLatch;

/**
 * RPC client application.
 */
@RpcServiceScan(basePackages = "org.example.rpc.**")
@SpringBootApplication(scanBasePackages = {"org.example.rpc.**"})
public class RpcClientApplication implements CommandLineRunner {

  @Autowired
  private SerializerFactory serializerFactory;

  @Autowired
  private NettyClient nettyClient;

  /**
   * Run RPC client.
   */
  public static void main(String[] args) {
    SpringApplication.run(RpcClientApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    new CountDownLatch(1).await();
  }
}
