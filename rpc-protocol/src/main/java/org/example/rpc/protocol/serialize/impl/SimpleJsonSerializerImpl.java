package org.example.rpc.protocol.serialize.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.example.rpc.protocol.serialize.Serializer;
import org.example.rpc.protocol.serialize.SerializerType;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

/**
 * Simple JSON serializer.
 */
@Slf4j
@Service
public class SimpleJsonSerializerImpl implements Serializer {

  private static final Logger logger = LoggerFactory.getLogger(SimpleJsonSerializerImpl.class);

  static {
    ParserConfig.getGlobalInstance().addAccept("org.example.rpc");
    SerializeConfig.getGlobalInstance().put(Throwable.class, new ThrowableSerializer());
    ParserConfig.getGlobalInstance().putDeserializer(Throwable.class, new ThrowableDeserializer());
    ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
  }

  @Override
  public SerializerType getSerializerType() {
    return SerializerType.JSON;
  }

  @Override
  public <T> byte[] serialize(T obj) throws Exception {
    return JSON.toJSONBytes(obj, SerializerFeature.WriteClassName);
  }

  @Override
  public <T> T deSerialize(byte[] bytes, Class<T> clazz) {
    String jsonString = new String(bytes);
    return JSON.parseObject(jsonString, clazz);
  }

  public static class ThrowableSerializer implements ObjectSerializer {
    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) {
      Throwable throwable = (Throwable) object;
      serializer.write(new ThrowableWrapper(throwable.getClass().getName(), throwable.getMessage()));
    }
  }

  public static class ThrowableDeserializer implements ObjectDeserializer {
    @Override
    public Throwable deserialze(com.alibaba.fastjson.parser.DefaultJSONParser parser, Type type, Object fieldName) {
      ThrowableWrapper wrapper = parser.parseObject(ThrowableWrapper.class);
      try {
        Class<?> clazz = Class.forName(wrapper.getClassName());
        return (Throwable) clazz.getConstructor(String.class).newInstance(wrapper.getMessage());
      } catch (Exception e) {
        return new RuntimeException(wrapper.getMessage());
      }
    }

    @Override
    public int getFastMatchToken() {
      return 0;
    }
  }

  @Getter
  @Setter
  public static class ThrowableWrapper {
    private String className;
    private String message;

    public ThrowableWrapper() {}

    public ThrowableWrapper(String className, String message) {
      this.className = className;
      this.message = message;
    }

  }
}
