package org.example.rpc.server;

import org.example.rpc.core.annotations.RpcServiceScan;
import org.example.rpc.core.serialize.SerializerFactory;
import org.example.rpc.core.server.NettyServer;
import org.example.rpc.core.server.NettyServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.CountDownLatch;

/**
 * RPC server application.
 */
@SuppressWarnings("ALL")
@RpcServiceScan(basePackages = "org.example.rpc.**")
@SpringBootApplication
public class RpcServerApplication implements CommandLineRunner {

  @Autowired
  private SerializerFactory serializerFactory;

  @Autowired
  private NettyServerProperties nettyServerProperties;

  /**
   * Run RPC server.
   */
  public static void main(String[] args) {
    SpringApplication.run(RpcServerApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    new NettyServer(nettyServerProperties.getServerPort(), serializerFactory);
    new CountDownLatch(1).await();
  }
}