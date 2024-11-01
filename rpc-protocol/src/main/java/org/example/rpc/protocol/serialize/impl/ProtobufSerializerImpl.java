package org.example.rpc.protocol.serialize.impl;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.example.rpc.protocol.model.RpcRequest;
import org.example.rpc.protocol.model.RpcResponse;
import org.example.rpc.protocol.model.packet.HeartBeatPacket;
import org.example.rpc.core.protocol.proto.RpcRequestProto;
import org.example.rpc.core.protocol.proto.RpcResponseProto;
import org.example.rpc.core.protocol.proto.ThrowableProto;
import org.example.rpc.protocol.serialize.Serializer;
import org.example.rpc.protocol.serialize.SerializerType;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <P>Protobuf serializer implementation.
 *
 * <p>Serialize and deserialize RpcRequest and RpcResponse using Protobuf.
 * This is the default serializer for RPC messages because it is more efficient than
 * directly sending JSON objects when they are large.
 *
 * @author Kunhua Huang
 */
@Slf4j
public class ProtobufSerializerImpl implements Serializer {

  @Override
  public SerializerType getSerializerType() {
    return SerializerType.PROTOBUF;
  }

  @Override
  public <T> byte[] serialize(T obj) throws Exception {
    if (obj instanceof HeartBeatPacket) {
      // 心跳包使用 JSON 序列化
      return JSON.toJSONBytes(obj);
    }
    if (obj instanceof RpcRequest) {
      return serializeRequest((RpcRequest) obj);
    } else if (obj instanceof RpcResponse) {
      return serializeResponse((RpcResponse) obj);
    }
    throw new IllegalArgumentException("Unsupported type: " + obj.getClass());
  }

  @Override
  public <T> T deSerialize(byte[] bytes, Class<T> clazz) throws Exception {
    if (HeartBeatPacket.class.isAssignableFrom(clazz)) {
      return JSON.parseObject(bytes, clazz);
    }
    if (RpcRequest.class.isAssignableFrom(clazz)) {
      return (T) deserializeRequest(bytes);
    } else if (RpcResponse.class.isAssignableFrom(clazz)) {
      return (T) deserializeResponse(bytes);
    }
    throw new IllegalArgumentException("Unsupported type: " + clazz);
  }

  private RpcRequest deserializeRequest(byte[] bytes) throws Exception {
    RpcRequestProto proto = RpcRequestProto.parseFrom(bytes);
    RpcRequest request = new RpcRequest();
    request.setSequence(proto.getSequence());
    request.setClassName(proto.getClassName());
    request.setMethodName(proto.getMethodName());
    request.setHttpMethod(proto.getHttpMethod());
    request.setPath(proto.getPath());

    if (proto.getParameterTypesCount() > 0) {
      Class<?>[] parameterTypes = new Class<?>[proto.getParameterTypesCount()];
      for (int i = 0; i < proto.getParameterTypesCount(); i++) {
        String typeName = proto.getParameterTypes(i);
        parameterTypes[i] = Class.forName(typeName);
      }
      request.setParameterTypes(parameterTypes);
    }

    if (proto.getParametersCount() > 0) {
      Map<String, Object> parameters = new HashMap<>();
      for (Map.Entry<String, ByteString> entry : proto.getParametersMap().entrySet()) {
        String jsonValue = entry.getValue().toStringUtf8();
        Class<?> paramType = null;

        if (entry.getKey().equals("body") && request.getParameterTypes() != null
            && request.getParameterTypes().length > 0) {
          paramType = request.getParameterTypes()[0];
          if (List.class.isAssignableFrom(paramType)) {
            Class<?> methodClass = Class.forName(request.getClassName());
            Method method = methodClass.getMethod(request.getMethodName(), paramType);
            Type genericParamType = method.getGenericParameterTypes()[0];

            if (genericParamType instanceof ParameterizedType) {
              parameters.put(entry.getKey(),
                  JSON.parseObject(jsonValue, genericParamType));
              continue;
            }
          }
        }

        Object value = paramType != null
            ? JSON.parseObject(jsonValue, paramType) :
            JSON.parse(jsonValue);
        parameters.put(entry.getKey(), value);
      }
      request.setParameters(parameters);
    }

    if (proto.getQueryParamsCount() > 0) {
      request.setQueryParams(proto.getQueryParamsMap());
    }

    return request;
  }

  private RpcResponse deserializeResponse(byte[] bytes) throws Exception {
    RpcResponseProto proto = RpcResponseProto.parseFrom(bytes);
    RpcResponse response = new RpcResponse();
    response.setSequence(proto.getSequence());

    if (proto.hasThrowable()) {
      ThrowableProto throwableProto = proto.getThrowable();
      Class<?> throwableClass = Class.forName(throwableProto.getClassName());
      Throwable throwable = (Throwable) throwableClass.getConstructor(String.class).newInstance(throwableProto.getMessage());
      response.setThrowable(throwable);
    }

    if (!proto.getResult().isEmpty()) {
      String jsonResult = proto.getResult().toStringUtf8();
      Class<?> returnType = proto.getReturnType().isEmpty() ? Object.class : Class.forName(proto.getReturnType());
      Object result = JSON.parseObject(jsonResult, returnType);
      response.setResult(result);
    }

    return response;
  }

  private byte[] serializeRequest(RpcRequest request) {
    RpcRequestProto.Builder builder = RpcRequestProto.newBuilder()
        .setSequence(request.getSequence())
        .setClassName(request.getClassName())
        .setMethodName(request.getMethodName())
        .setHttpMethod(request.getHttpMethod())
        .setPath(request.getPath());

    if (request.getParameterTypes() != null) {
      for (Class<?> parameterType : request.getParameterTypes()) {
        builder.addParameterTypes(parameterType.getName());
      }
    }

    if (request.getParameters() != null) {
      for (Map.Entry<String, Object> entry : request.getParameters().entrySet()) {
        if (entry.getValue() != null) {
          builder.putParameters(entry.getKey(), ByteString.copyFromUtf8(JSON.toJSONString(entry.getValue())));
        }
      }
    }

    if (request.getQueryParams() != null) {
      builder.putAllQueryParams(request.getQueryParams());
    }

    return builder.build().toByteArray();
  }

  private byte[] serializeResponse(RpcResponse response) {
    RpcResponseProto.Builder builder = RpcResponseProto.newBuilder()
        .setSequence(response.getSequence());

    if (response.getThrowable() != null) {
      Throwable throwable = response.getThrowable();
      ThrowableProto.Builder throwableBuilder = ThrowableProto.newBuilder()
          .setClassName(throwable.getClass().getName())
          .setMessage(throwable.getMessage() != null ? throwable.getMessage() : "")
          .setStackTrace(getStackTraceAsString(throwable));

      if (throwable.getCause() != null) {
        throwableBuilder.setCause(throwable.getCause().getMessage());
      }
      builder.setThrowable(throwableBuilder.build());
    }

    if (response.getResult() != null) {
      builder.setResult(ByteString.copyFromUtf8(JSON.toJSONString(response.getResult())))
          .setReturnType(response.getResult().getClass().getName());
    }

    return builder.build().toByteArray();
  }

  private String getStackTraceAsString(Throwable throwable) {
    StringBuilder sb = new StringBuilder();
    for (StackTraceElement element : throwable.getStackTrace()) {
      sb.append(element.toString()).append("\n");
    }
    return sb.toString();
  }
}