package org.example.rpc.blog;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.example.rpc.processor.RpcRequestProcessor;
import org.example.rpc.protocol.serialize.SerializerFactory;
import org.example.rpc.spring.annotation.RpcServiceScan;
import org.example.rpc.transport.server.NettyServer;
import org.example.rpc.transport.server.NettyServerProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * RPC server application.
 */
@SuppressWarnings("ALL")
@Slf4j
@RpcServiceScan(basePackages = "org.example.rpc.blog")
@SpringBootApplication(scanBasePackages = {
    "org.example.rpc.blog",
    "org.example.rpc.common",
    "org.example.rpc.spring",
    "org.example.rpc.registry",
    "org.example.rpc.processor",
    "org.example.rpc.protocol",
    "org.example.rpc.transport",
    "org.example.rpc.proxy",
    "org.example.rpc.network",
    "org.example.rpc.interceptor",
    "org.example.rpc.loadbalancer",
    "org.example.rpc.monitor",
    "org.example.rpc.router"
})
public class BlogServiceApplication implements CommandLineRunner {
  private final SerializerFactory serializerFactory;
  private final NettyServerProperties nettyServerProperties;
  private final RpcRequestProcessor requestProcessor;

  @Autowired
  public BlogServiceApplication(SerializerFactory serializerFactory,
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
    SpringApplication.run(BlogServiceApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    NettyServer nettyServer = new NettyServer(
        requestProcessor,
        nettyServerProperties,
        serializerFactory);
    new Thread(() -> {
      nettyServer.start();
      latch.countDown();
    }).start();
    latch.await();
  }
}
