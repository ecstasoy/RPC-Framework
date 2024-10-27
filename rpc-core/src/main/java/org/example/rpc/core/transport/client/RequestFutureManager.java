package org.example.rpc.core.transport.client;

import org.example.rpc.core.model.RpcRequest;
import org.example.rpc.core.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RequestFutureManager {

  private static final Map<String, CompletableFuture<RpcResponse>> RESPONSE_FUTURE_MAP = new ConcurrentHashMap<>();

  public static void addFuture(String sequence, CompletableFuture<RpcResponse> future) {
    RESPONSE_FUTURE_MAP.put(sequence, future);
  }

  public static void removeAndComplete(RpcResponse rpcResponse) {
    final String sequence = rpcResponse.getSequence();
    final CompletableFuture<RpcResponse> future = RESPONSE_FUTURE_MAP.remove(sequence);
    if (future == null) {
      log.info("Future [{}] not exist.", sequence);
    } else {
      future.complete(rpcResponse);
    }
  }
}
