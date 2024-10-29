package org.example.rpc.server;

import org.example.rpc.core.common.annotations.RpcServiceScan;
import org.example.rpc.core.process.RpcRequestProcessor;
import org.example.rpc.core.protocol.serialize.SerializerFactory;
import org.example.rpc.core.transport.server.NettyServer;
import org.example.rpc.core.transport.server.NettyServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import java.util.concurrent.CountDownLatch;

/**
 * RPC server application.
 */
@SuppressWarnings("ALL")
@RpcServiceScan(basePackages = "org.example.rpc.**")
@SpringBootApplication(scanBasePackages = "org.example.rpc.**")
public class RpcServerApplication implements CommandLineRunner {

  @Autowired
  private SerializerFactory serializerFactory;

  @Autowired
  private NettyServerProperties nettyServerProperties;

  @Autowired
  private RpcRequestProcessor requestProcessor;

  /**
   * Run RPC server.
   */
  public static void main(String[] args) {
    SpringApplication.run(RpcServerApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    NettyServer nettyServer = new NettyServer(requestProcessor, nettyServerProperties, serializerFactory);
    new Thread(() -> {
      nettyServer.start();
      latch.countDown();
    }).start();
    latch.await();
  }
}
