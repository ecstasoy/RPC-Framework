package org.example.rpc.user;

import lombok.extern.slf4j.Slf4j;
import org.example.rpc.processor.RpcRequestProcessor;
import org.example.rpc.protocol.serialize.SerializerFactory;
import org.example.rpc.spring.annotation.RpcServiceScan;
import org.example.rpc.transport.server.NettyServer;
import org.example.rpc.transport.server.NettyServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.CompletableFuture;

/**
 * RPC server application.
 */
@SuppressWarnings("ALL")
@Slf4j
@RpcServiceScan(basePackages = "org.example.rpc.**")
@SpringBootApplication(scanBasePackages = {"org.example.rpc"})
public class UserServiceApplication implements CommandLineRunner {
  private final SerializerFactory serializerFactory;
  private final NettyServerProperties nettyServerProperties;
  private final RpcRequestProcessor requestProcessor;

  @Autowired
  public UserServiceApplication(SerializerFactory serializerFactory,
                                NettyServerProperties nettyServerProperties,
                                RpcRequestProcessor requestProcessor) {
    this.serializerFactory = serializerFactory;
    this.nettyServerProperties = nettyServerProperties;
    this.requestProcessor = requestProcessor;
  }

  /**
   * Run RPC server.
   */
  public static void main(String[] args) {
    SpringApplication.run(UserServiceApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    CompletableFuture.runAsync(() -> {
      try {
        NettyServer nettyServer = new NettyServer(
            requestProcessor,
            nettyServerProperties,
            serializerFactory
        );
        nettyServer.start();
        log.info("Netty server started successfully on port: {}", nettyServerProperties.getServerPort());
      } catch (Exception e) {
        log.error("Failed to start netty server", e);
        System.exit(1);
      }
    }).exceptionally(throwable -> {
      log.error("Unexpected error during server startup", throwable);
      System.exit(1);
      return null;
    });
  }
}
