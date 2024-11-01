package org.example.rpc.core.test.heartbeat;

import lombok.extern.slf4j.Slf4j;
import org.example.rpc.common.exception.RpcException;
import org.example.rpc.core.test.TestConfig;
import org.example.rpc.core.test.TestService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {TestConfig.class})
@Slf4j
public class HeartbeatTest {
  @Autowired
  private TestService testService;

  @Test
  public void testHeartbeat() throws InterruptedException {
    // 1. 正常调用，确保连接建立
    String result = testService.echo("test");
    Assertions.assertEquals("Echo: test", result);

    // 2. 等待超过写空闲时间，触发心跳
    Thread.sleep(15000); // 等待15秒，应该会触发至少一次心跳

    // 3. 再次调用，确认连接仍然可用
    result = testService.echo("test-after-heartbeat");
    Assertions.assertEquals("Echo: test-after-heartbeat", result);

    // 4. 模拟服务端不响应
    Thread.sleep(35000); // 等待35秒，超过读空闲时间

    // 5. 此时应该抛出连接异常
    RpcException exception = Assertions.assertThrows(RpcException.class, () -> {
      testService.echo("test-after-connection-lost");
    });
  }
}