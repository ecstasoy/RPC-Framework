package org.example.rpc.core.test.loadbalance;

import org.example.rpc.core.loadbalance.LoadBalancerFactory;
import org.example.rpc.core.loadbalance.api.LoadBalanceStrategy;
import org.example.rpc.core.loadbalance.api.LoadBalancer;
import org.example.rpc.core.test.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest(
    classes = TestConfig.class  // 明确指定使用哪个配置类
)
public class LoadBalancerTest {

  private static final String SERVICE_NAME = "test.service";
  @Autowired
  private LoadBalancerFactory loadBalancerFactory;
  private List<String> instances;

  @BeforeEach
  void setUp() {
    instances = Arrays.asList(
        "instance1:8080",
        "instance2:8080",
        "instance3:8080"
    );
  }

  @Test
  void testRandomLoadBalancer() {
    LoadBalancer loadBalancer = loadBalancerFactory.getLoadBalancer(LoadBalanceStrategy.RANDOM);
    Map<String, AtomicInteger> selectionCount = new ConcurrentHashMap<>();
    int totalCalls = 10000; // 增加调用次数以获得更好的统计结果

    for (int i = 0; i < totalCalls; i++) {
      String selected = loadBalancer.select(instances, SERVICE_NAME);
      assertNotNull(selected, "Selected instance should not be null");
      selectionCount.computeIfAbsent(selected, k -> new AtomicInteger(0)).incrementAndGet();
    }

    // 验证分布是否相对均匀（允许15%的误差）
    int expectedCount = totalCalls / instances.size();
    double allowedDeviation = 0.15;

    for (String instance : instances) {
      int count = selectionCount.get(instance).get();
      assertTrue(Math.abs(count - expectedCount) <= expectedCount * allowedDeviation,
          String.format("Instance %s selected %d times, expected around %d (±%d%%)",
              instance, count, expectedCount, (int) (allowedDeviation * 100)));
    }
  }

  @Test
  void testRoundRobinLoadBalancer() {
    LoadBalancer loadBalancer = loadBalancerFactory.getLoadBalancer(LoadBalanceStrategy.ROUND_ROBIN);
    List<String> selections = new ArrayList<>();
    int rounds = 3;

    // 测试多轮完整轮询
    for (int round = 0; round < rounds; round++) {
      for (int i = 0; i < instances.size(); i++) {
        String selected = loadBalancer.select(instances, SERVICE_NAME);
        selections.add(selected);
      }
    }

    // 验证轮询序列的正确性
    for (int i = 0; i < selections.size(); i++) {
      assertEquals(instances.get(i % instances.size()), selections.get(i),
          String.format("Wrong selection at position %d", i));
    }
  }

  @Test
  void testConcurrentRoundRobin() throws InterruptedException {
    int threadCount = 10;
    int requestsPerThread = 30; // 确保能够整除实例数量
    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    Map<String, AtomicInteger> selectionCount = new ConcurrentHashMap<>();

    for (int i = 0; i < threadCount; i++) {
      executorService.submit(() -> {
        try {
          for (int j = 0; j < requestsPerThread; j++) {
            String selected = loadBalancerFactory.getLoadBalancer(LoadBalanceStrategy.ROUND_ROBIN).select(instances, SERVICE_NAME);
            assertNotNull(selected);
            selectionCount.computeIfAbsent(selected, k -> new AtomicInteger(0))
                .incrementAndGet();
          }
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();
    executorService.shutdown();

    // 验证每个实例被调用的次数是否相等
    int expectedCount = (threadCount * requestsPerThread) / instances.size();
    for (String instance : instances) {
      assertEquals(expectedCount, selectionCount.get(instance).get(),
          String.format("Instance %s was not selected the expected number of times", instance));
    }
  }

  @Test
  void testEmptyInstances() {
    assertNull(loadBalancerFactory.getLoadBalancer(LoadBalanceStrategy.RANDOM).select(Collections.emptyList(), SERVICE_NAME));
    assertNull(loadBalancerFactory.getLoadBalancer(LoadBalanceStrategy.ROUND_ROBIN).select(Collections.emptyList(), SERVICE_NAME));
    assertNull(loadBalancerFactory.getLoadBalancer(LoadBalanceStrategy.WEIGHTED).select(Collections.emptyList(), SERVICE_NAME));
    assertNull(loadBalancerFactory.getLoadBalancer(LoadBalanceStrategy.LEAST_ACTIVE).select(Collections.emptyList(), SERVICE_NAME));
  }

  @Test
  void testWeightedLoadBalancer() {
    String serviceName = "weighted.test.service";
    Map<String, AtomicInteger> selectionCount = new ConcurrentHashMap<>();
    int totalCalls = 1000;

    // First round with default weights
    for (int i = 0; i < totalCalls; i++) {
      String selected = loadBalancerFactory.getLoadBalancer(LoadBalanceStrategy.WEIGHTED).select(instances, serviceName);
      assertNotNull(selected);
      selectionCount.computeIfAbsent(selected, k -> new AtomicInteger(0)).incrementAndGet();
    }

    // Verify initial distribution is roughly equal
    int expectedCount = totalCalls / instances.size();
    double allowedDeviation = 0.15;

    for (String instance : instances) {
      int count = selectionCount.get(instance).get();
      assertTrue(Math.abs(count - expectedCount) <= expectedCount * allowedDeviation,
          String.format("Instance %s selected %d times, expected around %d (±%d%%)",
              instance, count, expectedCount, (int) (allowedDeviation * 100)));
    }

    // Clear counts for next test
    selectionCount.clear();

    // Adjust weights
    loadBalancerFactory.adjustWeight(LoadBalanceStrategy.WEIGHTED, serviceName, "instance1:8080", 20);
    loadBalancerFactory.adjustWeight(LoadBalanceStrategy.WEIGHTED, serviceName, "instance2:8080", 10);
    loadBalancerFactory.adjustWeight(LoadBalanceStrategy.WEIGHTED, serviceName, "instance3:8080", 5);

    // Second round with adjusted weights
    for (int i = 0; i < totalCalls; i++) {
      String selected = loadBalancerFactory.getLoadBalancer(LoadBalanceStrategy.WEIGHTED).select(instances, serviceName);
      assertNotNull(selected);
      selectionCount.computeIfAbsent(selected, k -> new AtomicInteger(0)).incrementAndGet();
    }

    // Verify weighted distribution
    int count1 = selectionCount.get("instance1:8080").get();
    int count2 = selectionCount.get("instance2:8080").get();
    int count3 = selectionCount.get("instance3:8080").get();

    // Allow 20% deviation from expected ratios
    double tolerance = 0.2;

    // Verify weight ratios (20:10:5)
    assertTrue(Math.abs((double) count1 / count2 - 2.0) <= tolerance,
        String.format("Ratio between instance1 and instance2 should be close to 2.0, but was %.2f", (double) count1 / count2));
    assertTrue(Math.abs((double) count1 / count3 - 4.0) <= tolerance,
        String.format("Ratio between instance1 and instance3 should be close to 4.0, but was %.2f", (double) count1 / count3));
    assertTrue(Math.abs((double) count2 / count3 - 2.0) <= tolerance,
        String.format("Ratio between instance2 and instance3 should be close to 2.0, but was %.2f", (double) count2 / count3));
  }

  @Test
  void testLeastActiveLoadBalancer() {
    String instance1 = instances.get(0);
    String instance2 = instances.get(1);
    String instance3 = instances.get(2);

    // 1. 初始状态，所有实例活跃度都是0，应该随机选择
    Map<String, Integer> selectionCount = new HashMap<>();
    for (int i = 0; i < 100; i++) {
      String selected = loadBalancerFactory.getLoadBalancer(LoadBalanceStrategy.LEAST_ACTIVE).select(instances, SERVICE_NAME);
      selectionCount.merge(selected, 1, Integer::sum);
    }
    // 验证所有实例都被选择过
    assertEquals(3, selectionCount.size(), "All instances should be selected");

    // 2. 减少instance2的活跃度
    for (int i = 0; i < 3; i++) {
      loadBalancerFactory.getLoadBalancer(LoadBalanceStrategy.LEAST_ACTIVE).decrementActive(instance2);
    }

    // instance2应该被优先选择
    String selected = loadBalancerFactory.getLoadBalancer(LoadBalanceStrategy.LEAST_ACTIVE).select(instances, SERVICE_NAME);
    assertEquals(instance2, selected, "Should select instance with least active count");

    // 3. 验证活跃度会自动增加
    Map<String, AtomicInteger> activeCount = new HashMap<>();
    for (int i = 0; i < 10; i++) {
      String instance = loadBalancerFactory.getLoadBalancer(LoadBalanceStrategy.LEAST_ACTIVE).select(instances, SERVICE_NAME);
      activeCount.computeIfAbsent(instance, k -> new AtomicInteger(0)).incrementAndGet();
    }

    // 验证负载是否分散
    assertTrue(activeCount.size() > 1, "Load should be distributed among instances");
  }

  @Test
  void testConcurrentWeightedLoadBalancer() throws InterruptedException {
    int threadCount = 10;
    int requestsPerThread = 100;
    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    Map<String, AtomicInteger> selectionCount = new ConcurrentHashMap<>();

    for (int i = 0; i < threadCount; i++) {
      executorService.submit(() -> {
        try {
          for (int j = 0; j < requestsPerThread; j++) {
            String selected = loadBalancerFactory.getLoadBalancer(LoadBalanceStrategy.WEIGHTED).select(instances, SERVICE_NAME);
            assertNotNull(selected);
            selectionCount.computeIfAbsent(selected, k -> new AtomicInteger(0))
                .incrementAndGet();
          }
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();
    executorService.shutdown();

    // 验证并发情况下权重分配是否正确
    int totalRequests = threadCount * requestsPerThread;
    for (String instance : instances) {
      assertTrue(selectionCount.containsKey(instance),
          "Instance " + instance + " should be selected");
      int count = selectionCount.get(instance).get();
      assertTrue(count > 0, "Instance " + instance + " should be selected at least once");
    }
  }

  private void decrementActive(String instance) {
    loadBalancerFactory.decrementActive(LoadBalanceStrategy.LEAST_ACTIVE, instance);
  }

  @Test
  void testConsistentHashLoadBalancer() {
    LoadBalancer loadBalancer = loadBalancerFactory.getLoadBalancer(LoadBalanceStrategy.CONSISTENT_HASH);

    // Test same parameter always returns the same instance
    String param1 = "test-param-1";
    String param2 = "test-param-2";
    String serviceNameWithParam1 = SERVICE_NAME + "#" + param1;
    String serviceNameWithParam2 = SERVICE_NAME + "#" + param2;

    String selected1 = loadBalancer.select(instances, serviceNameWithParam1);
    String selected2 = loadBalancer.select(instances, serviceNameWithParam2);

    // Multiple calls with the same parameter should return the same instance
    for (int i = 0; i < 10; i++) {
      assertEquals(selected1, loadBalancer.select(instances, serviceNameWithParam1),
          "Same parameter should always return the same instance");
      assertEquals(selected2, loadBalancer.select(instances, serviceNameWithParam2),
          "Same parameter should always return the same instance");
    }

    // Test redistribution after instance removal
    List<String> reducedInstances = new ArrayList<>(instances);
    reducedInstances.remove(selected1);

    String newSelected = loadBalancer.select(reducedInstances, serviceNameWithParam1);
    assertNotEquals(selected1, newSelected,
        "Should select different instance after removal");

    // Test distribution uniformity
    Map<String, AtomicInteger> distribution = new ConcurrentHashMap<>();
    int totalRequests = 10000;

    for (int i = 0; i < totalRequests; i++) {
      String param = "test-param-" + i;
      String serviceNameWithParam = SERVICE_NAME + "#" + param;
      String selected = loadBalancer.select(instances, serviceNameWithParam);
      distribution.computeIfAbsent(selected, k -> new AtomicInteger(0))
          .incrementAndGet();
    }

    // Check if distribution is roughly equal, 10% deviation allowed
    int expectedCount = totalRequests / instances.size();
    double allowedDeviation = 0.1;

    for (String instance : instances) {
      int count = distribution.get(instance).get();
      assertTrue(Math.abs(count - expectedCount) <= expectedCount * allowedDeviation,
          String.format("Instance %s got %d requests, expected around %d (±%d%%)",
              instance, count, expectedCount, (int)(allowedDeviation * 100)));
    }
  }
}
