package io.ecstasoy.rpc.bff;

import io.ecstasoy.rpc.spring.annotation.RpcServiceScan;
import io.ecstasoy.rpc.transport.client.NettyClient;
import io.ecstasoy.rpc.protocol.serialize.SerializerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.concurrent.CountDownLatch;

/**
 * RPC client application.
 */
@RpcServiceScan(basePackages = "io.ecstasoy.rpc.**")
@SpringBootApplication(scanBasePackages = {"io.ecstasoy.rpc.**"})
public class BffServiceApplication implements CommandLineRunner {

  @Autowired
  private SerializerFactory serializerFactory;

  @Autowired
  private NettyClient nettyClient;

  /**
   * Run RPC client.
   */
  public static void main(String[] args) {
    SpringApplication.run(BffServiceApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    new CountDownLatch(1).await();
  }
}
