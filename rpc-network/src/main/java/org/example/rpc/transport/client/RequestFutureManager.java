package org.example.rpc.transport.client;

import lombok.extern.slf4j.Slf4j;
import org.example.rpc.protocol.model.RpcResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to manage request futures.
 *
 * <p>Stores and manages the futures of requests sent by the client.
 *
 * @author Kunhua Huang
 */
@Slf4j
public class RequestFutureManager {

  private static final Map<String, CompletableFuture<RpcResponse>> RESPONSE_FUTURE_MAP = new ConcurrentHashMap<>();

  /**
   * Add a future to the map.
   *
   * @param sequence the sequence of the request
   * @param future the future of the request
   */
  public static void addFuture(String sequence, CompletableFuture<RpcResponse> future) {
    RESPONSE_FUTURE_MAP.put(sequence, future);
  }

  /**
   * Remove the future from the map and complete it.
   *
   * @param rpcResponse the response of the request
   */
  public static void removeAndComplete(RpcResponse rpcResponse) {
    final String sequence = rpcResponse.getSequence();
    final CompletableFuture<RpcResponse> future = RESPONSE_FUTURE_MAP.remove(sequence);
    if (future == null) {
      log.warn("Future [{}] not exist.", sequence);
    } else {
      future.complete(rpcResponse);
      log.debug("Complete future [{}].", sequence);
    }
  }

  /**
   * Remove the future from the map.
   *
   * @param sequence the sequence of the request
   * @return the future of the request
   */
  public static CompletableFuture<RpcResponse> removeFuture(String sequence) {
    return RESPONSE_FUTURE_MAP.remove(sequence);
  }
}
