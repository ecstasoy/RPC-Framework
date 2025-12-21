package io.ecstasoy.rpc.spring;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import io.ecstasoy.rpc.common.annotations.Reference;
import io.ecstasoy.rpc.common.annotations.RpcService;
import io.ecstasoy.rpc.network.RpcRequestSender;
import io.ecstasoy.rpc.proxy.RpcClientProxy;
import io.ecstasoy.rpc.registry.registry.api.RpcServiceRegistry;
import io.ecstasoy.rpc.registry.registry.param.RpcServiceRegistryParam;
import io.ecstasoy.rpc.transport.server.NettyServerProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.net.InetAddress;

/**
 * Class to process RPC-related beans.
 */
@Component
@Slf4j
public class RpcBeanProcessor implements BeanPostProcessor {
  private final RpcRequestSender requestSender;
  private final RpcServiceRegistry rpcServiceRegistry;
  private final NettyServerProperties nettyServerProperties;

  @Value("${rpc.service.name:}")
  private String currentServiceName;

  /**
   * Constructor.
   *
   * @param rpcServiceRegistry    RPC service registry
   * @param requestSender         RPC request sender
   * @param nettyServerProperties Netty server properties
   */
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

    RpcService rpcServiceAnnotation = bean.getClass().getAnnotation(RpcService.class);

    if (rpcServiceAnnotation == null) {
      return bean;
    }

    Class<?>[] interfaces = bean.getClass().getInterfaces();
    if (interfaces.length == 0) {
      log.warn("Bean [{}] does not implement any interfaces, skipping service registration.", beanName);
      return bean;
    }

    String serviceName = interfaces[0].getCanonicalName();
    if (!currentServiceName.isEmpty() && !serviceName.contains(currentServiceName)) {
      log.debug("Skip registering service [{}] as it's not the current service", serviceName);
      return bean;
    }

    RpcServiceRegistryParam registryParam = new RpcServiceRegistryParam();
    registryParam.setServiceName(interfaces[0].getCanonicalName());
    registryParam.setIp(InetAddress.getLocalHost().getHostAddress());
    registryParam.setPort(nettyServerProperties.getServerPort());
    registryParam.setRpcBean(bean);
    registryParam.setInstanceId(registryParam.getServiceName() + "#" + registryParam.getIp() + ":" + registryParam.getPort());

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
      field.setAccessible(true);
      try {
        field.set(bean, proxy);
        log.info("Successfully injected RPC proxy for field: [{}]", field.getName());
      } catch (IllegalAccessException e) {
        throw new RuntimeException("Property [" + field.getName() + "] is not accessible", e);
      }
    }

    return bean;
  }
}
