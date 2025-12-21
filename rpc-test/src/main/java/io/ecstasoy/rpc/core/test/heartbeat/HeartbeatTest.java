package io.ecstasoy.rpc.core.test.heartbeat;

import lombok.extern.slf4j.Slf4j;
import io.ecstasoy.rpc.common.exception.RpcException;
import io.ecstasoy.rpc.core.test.TestConfig;
import io.ecstasoy.rpc.core.test.TestService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Heartbeat test.
 *
 * <p>Test the heartbeat mechanism.
 *
 * @author Kunhua Huang
 */
@SpringBootTest(classes = {TestConfig.class})
@Slf4j
public class HeartbeatTest {

  @Autowired
  private TestService testService;

  @Test
  public void testHeartbeat() throws InterruptedException {

    String result = testService.echo("test");
    Assertions.assertEquals("Echo: test", result);
    log.debug("Echo result: {}", result);

    Thread.sleep(15000); // Trigger heartbeat

    result = testService.echo("test-after-heartbeat");
    Assertions.assertEquals("Echo: test-after-heartbeat", result);
    log.debug("Echo result: {}", result);

    Thread.sleep(35000); // To reach the timeout of the second heartbeat

    RpcException exception = Assertions.assertThrows(RpcException.class, () -> testService.echo("test-after-connection-lost"));
    log.error("Exception: ", exception);
  }
}