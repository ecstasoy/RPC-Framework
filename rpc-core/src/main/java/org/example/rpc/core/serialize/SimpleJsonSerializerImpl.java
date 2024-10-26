package org.example.rpc.core.serialize;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;

/**
 * Simple JSON serializer.
 */
@Service
public class SimpleJsonSerializerImpl implements Serializer {

  /**
   * Register classes.
   */
  static {
    ParserConfig.getGlobalInstance().addAccept("org.example.rpc");
    SerializeConfig.getGlobalInstance().put(Throwable.class, new ThrowableSerializer());
    ParserConfig.getGlobalInstance().putDeserializer(Throwable.class, new ThrowableDeserializer());
  }

  @Override
  public byte[] serialize(Object obj) {
    return JSON.toJSONBytes(obj, SerializerFeature.WriteClassName);
  }

  @Override
  public <T>T deSerialize(byte[] bytes, Class<T> classType) {
    return JSON.parseObject(bytes, classType);
  }

  @Override
  public SerializerType getSerializerType() {
    return SerializerType.JSON;
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

  public static class ThrowableWrapper {
    private String className;
    private String message;

    public ThrowableWrapper() {}

    public ThrowableWrapper(String className, String message) {
      this.className = className;
      this.message = message;
    }

    public String getClassName() {
      return className;
    }

    public void setClassName(String className) {
      this.className = className;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }
  }
}
