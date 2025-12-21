package io.ecstasoy.rpc.core.test.loadbalance;

import io.ecstasoy.rpc.core.test.TestConfig;
import io.ecstasoy.rpc.loadbalancer.LoadBalancerFactory;
import io.ecstasoy.rpc.loadbalancer.api.LoadBalanceStrategy;
import io.ecstasoy.rpc.loadbalancer.api.LoadBalancer;
import io.ecstasoy.rpc.registry.discovery.api.RpcServiceDiscovery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
    classes = TestConfig.class
)
public class LoadBalanceIntegrationTest {

  @Autowired
  private LoadBalancerFactory loadBalancerFactory;

  @Autowired
  private RpcServiceDiscovery serviceDiscovery;

  @Test
  void testLoadBalancerWithMultipleInstances() throws Exception {
    String serviceName = "test.core.rpc.io.ecstasoy.TestService";
    List<String> instances = serviceDiscovery.getServiceInstaceList(serviceName);

    testStrategy(LoadBalanceStrategy.ROUND_ROBIN, instances, serviceName);
    testStrategy(LoadBalanceStrategy.RANDOM, instances, serviceName);
    testStrategy(LoadBalanceStrategy.WEIGHTED, instances, serviceName);
    testStrategy(LoadBalanceStrategy.LEAST_ACTIVE, instances, serviceName);
    testStrategy(LoadBalanceStrategy.CONSISTENT_HASH, instances, serviceName);
  }

  private void testStrategy(LoadBalanceStrategy strategy, List<String> instances, String serviceName) throws InterruptedException {
    LoadBalancer loadBalancer = loadBalancerFactory.getLoadBalancer(strategy);
    Map<String, AtomicInteger> distribution = new ConcurrentHashMap<>();

    // 模拟大量请求
    int requests = 100000;
    CountDownLatch latch = new CountDownLatch(requests);
    ExecutorService executor = Executors.newFixedThreadPool(10);

    for (int i = 0; i < requests; i++) {
      executor.submit(() -> {
        try {
          String selected = loadBalancer.select(instances, serviceName);
          distribution.computeIfAbsent(selected, k -> new AtomicInteger(0))
              .incrementAndGet();
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();
    executor.shutdown();

    analyzeDistribution(distribution, strategy);
  }

  private void analyzeDistribution(Map<String, AtomicInteger> distribution, LoadBalanceStrategy strategy) {
    System.out.println("\n----------------------------------------");
    System.out.println("Load balance strategy: " + strategy);
    distribution.forEach((k, v) -> {
      System.out.printf("Instance %s: %d requests (%.2f%%)\n",
          k, v.get(), (v.get() * 100.0 / 100000));
    });
    System.out.println("----------------------------------------\n");
  }

  @Test
  void performanceTest() throws ExecutionException {
    String serviceName = "test.core.rpc.io.ecstasoy.TestService";
    List<String> instances = serviceDiscovery.getServiceInstaceList(serviceName);

    for (LoadBalanceStrategy strategy : LoadBalanceStrategy.values()) {
      LoadBalancer loadBalancer = loadBalancerFactory.getLoadBalancer(strategy);

      // 预热
      for (int i = 0; i < 100000; i++) {
        loadBalancer.select(instances, serviceName);
      }

      // 性能测试
      long startTime = System.nanoTime();
      for (int i = 0; i < 1000000; i++) {
        loadBalancer.select(instances, serviceName);
      }
      long endTime = System.nanoTime();

      double avgTime = (endTime - startTime) / 100000.0;
      System.out.printf("Strategy: %s, Average selection time: %.2f ns%n", strategy, avgTime);
    }
  }
}
