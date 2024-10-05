package org.example.rpc.core.spring;

import org.example.rpc.core.annotations.RpcService;
import org.example.rpc.core.annotations.Reference;
import org.example.rpc.core.client.RpcClientProxy;
import org.example.rpc.core.network.RpcRequestSender;
import org.example.rpc.core.registry.RpcServiceRegistry;
import org.example.rpc.core.registry.param.RpcServiceRegistryParam;
import org.example.rpc.core.server.NettyServerProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.net.InetAddress;

@Component
@Slf4j
public class RpcBeanProcessor implements BeanPostProcessor {
   private final RpcRequestSender requestSender;
   private final RpcServiceRegistry rpcServiceRegistry;
   private final NettyServerProperties nettyServerProperties;

  public RpcBeanProcessor(RpcServiceRegistry rpcServiceRegistry,
                          RpcRequestSender requestSender,
                          NettyServerProperties nettyServerProperties) {
    this.rpcServiceRegistry = rpcServiceRegistry;
    this.requestSender = requestSender;
    this.nettyServerProperties = nettyServerProperties;
  }

  @SneakyThrows
  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName)
      throws BeansException {

    Service rpcServiceAnnotation = bean.getClass().getAnnotation(Service.class);

    if (rpcServiceAnnotation == null) {
      return bean;
    }

    RpcServiceRegistryParam registryParam = new RpcServiceRegistryParam();
    registryParam.setServiceName(bean.getClass().getInterfaces()[0].getCanonicalName());
    registryParam.setIp(InetAddress.getLocalHost().getHostAddress());
    registryParam.setPort(nettyServerProperties.getServerPort());
    registryParam.setRpcBean(bean);

    rpcServiceRegistry.register(registryParam);
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    Class<?> beanClass = bean.getClass();
    Field[] declaredFields = beanClass.getDeclaredFields();

    for (Field field : declaredFields) {
      Reference annotation = field.getAnnotation(Reference.class);
      if (annotation == null) {
        continue;
      }

      log.info("Field: [{}] is annotated with [{}]", field.getName(), beanClass.getName());

      final RpcClientProxy rpcClientProxy = new RpcClientProxy(requestSender);
      final Object proxy = rpcClientProxy.getProxy(field.getType());
      field.setAccessible(true);try {
        field.set(bean, proxy);
      } catch (IllegalAccessException e) {
        throw new RuntimeException("Property [" + field.getName() + "] is not accessible", e);
      }
    }

    return bean;
  }
}
