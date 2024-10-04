package org.example.rpc.core.serialize;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.stereotype.Service;

public class SimpleJsonSerializerImpl implements Serializer{

  static{
    ParserConfig.getGlobalInstance().addAccept("org.example.rpc");
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
}
