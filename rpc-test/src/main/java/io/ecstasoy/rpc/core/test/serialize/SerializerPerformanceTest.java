package io.ecstasoy.rpc.core.test.serialize;

import lombok.extern.slf4j.Slf4j;
import io.ecstasoy.rpc.protocol.model.RpcRequest;
import io.ecstasoy.rpc.protocol.serialize.Serializer;
import io.ecstasoy.rpc.protocol.serialize.impl.ProtobufSerializerImpl;
import io.ecstasoy.rpc.protocol.serialize.impl.SimpleJsonSerializerImpl;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class SerializerPerformanceTest {

  private static final int WARMUP_ITERATIONS = 10000;
  private static final int TEST_ITERATIONS = 100000;

  @Test
  public void compareSerializerPerformance() throws Exception {
    RpcRequest request = createSampleRequest();

    Serializer protobufSerializer = new ProtobufSerializerImpl();
    Serializer jsonSerializer = new SimpleJsonSerializerImpl();

    // Prevent auto-optimization by JIT compiler
    for (int i = 0; i < WARMUP_ITERATIONS; i++) {
      byte[] protobufBytes = protobufSerializer.serialize(request);
      protobufSerializer.deSerialize(protobufBytes, RpcRequest.class);

      byte[] jsonBytes = jsonSerializer.serialize(request);
      jsonSerializer.deSerialize(jsonBytes, RpcRequest.class);
    }

    long protobufStart = System.nanoTime();
    for (int i = 0; i < TEST_ITERATIONS; i++) {
      byte[] bytes = protobufSerializer.serialize(request);
      protobufSerializer.deSerialize(bytes, RpcRequest.class);
    }
    long protobufTime = System.nanoTime() - protobufStart;
    log.info("Protobuf serializing + deserializing time: {} ns", protobufTime);

    long jsonStart = System.nanoTime();
    for (int i = 0; i < TEST_ITERATIONS; i++) {
      byte[] bytes = jsonSerializer.serialize(request);
      jsonSerializer.deSerialize(bytes, RpcRequest.class);
    }
    long jsonTime = System.nanoTime() - jsonStart;
    log.info("JSON serializing + deserializing time: {} ns", jsonTime);

    double protobufAvg = protobufTime / (double) TEST_ITERATIONS;
    double jsonAvg = jsonTime / (double) TEST_ITERATIONS;

    log.info("Protobuf serializing + deserializing avg time: {} ns", String.format("%.2f", protobufAvg));
    log.info("JSON serializing + deserializing avg time: {} ns", String.format("%.2f", jsonAvg));
    log.info("Protobuf improvement of speed compared to JSON: {}%",
        String.format("%.2f", ((jsonAvg - protobufAvg) / jsonAvg) * 100));

    byte[] protobufBytes = protobufSerializer.serialize(request);
    byte[] jsonBytes = jsonSerializer.serialize(request);
    log.info("Protobuf size after serializing: {} bytes", protobufBytes.length);
    log.info("JSON size after serializing: {} bytes", jsonBytes.length);
    log.info("Protobuf reduction of object size comared to JSON: {}%",
        String.format("%.2f", ((jsonBytes.length - protobufBytes.length) / (double) jsonBytes.length) * 100));
  }

  private RpcRequest createSampleRequest() {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("id", 1);
    parameters.put("name", "test");
    parameters.put("data", Arrays.asList(1, 2, 3, 4, 5));

    return RpcRequest.builder()
        .sequence(UUID.randomUUID().toString())
        .className("com.example.TestService")
        .methodName("testMethod")
        .parameterTypes(new Class<?>[]{String.class, Integer.class})
        .parameters(parameters)
        .httpMethod("POST")
        .path("/api/test")
        .build();
  }
}