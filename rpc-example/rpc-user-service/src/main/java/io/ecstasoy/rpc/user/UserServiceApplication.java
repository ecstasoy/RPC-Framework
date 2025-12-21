package io.ecstasoy.rpc.user;

import lombok.extern.slf4j.Slf4j;
import io.ecstasoy.rpc.processor.RpcRequestProcessor;
import io.ecstasoy.rpc.protocol.serialize.SerializerFactory;
import io.ecstasoy.rpc.spring.annotation.RpcServiceScan;
import io.ecstasoy.rpc.transport.server.NettyServer;
import io.ecstasoy.rpc.transport.server.NettyServerProperties;
import org.mybatis.spring.annotation.MapperScan;
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
@RpcServiceScan(basePackages = "io.ecstasoy.rpc.**")
@SpringBootApplication(scanBasePackages = {"io.ecstasoy.rpc"})
@MapperScan("io.ecstasoy.rpc.user.mapper")
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
