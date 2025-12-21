package io.ecstasoy.rpc.spring;

import lombok.extern.slf4j.Slf4j;
import io.ecstasoy.rpc.common.annotations.RpcService;
import io.ecstasoy.rpc.spring.annotation.RpcServiceScan;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;

/**
 * Rpc bean registrar.
 */
@Slf4j
public class RpcBeanRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

  /**
   * rpc-core package full path.
   */
  public static final String CORE_MODULE_PACKAGE = "io.ecstasoy.rpc";

  @Override
  public void setResourceLoader(ResourceLoader resourceLoader) {
  }

  @Override
  public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                      BeanDefinitionRegistry registry) {
    AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(importingClassMetadata
        .getAnnotationAttributes(RpcServiceScan.class.getName()));
    String[] rpcPakages = new String[0];

    if (annotationAttributes != null) {
      rpcPakages = annotationAttributes.getStringArray("basePackages");
    }

    if (rpcPakages.length == 0) {
      // throw new IllegalArgumentException("rpcPackages can not be empty");
      rpcPakages = new String[]{((StandardAnnotationMetadata) importingClassMetadata)
          .getIntrospectedClass().getPackage().getName()};
    }

    RpcBeanDefinitionScanner serviceScanner = new RpcBeanDefinitionScanner(registry,
        RpcService.class);

    serviceScanner.scan(CORE_MODULE_PACKAGE);

    int rpcServiceNum = serviceScanner.scan(rpcPakages);
    log.info("Rpc Service registered number: [{}]", rpcServiceNum);
  }

  /**
   * Scanner for RpcService.
   */
  static class RpcBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {

    public RpcBeanDefinitionScanner(BeanDefinitionRegistry registry,
                                    Class<? extends Annotation> classType) {
      super(registry);
      addIncludeFilter(new AnnotationTypeFilter(classType));
    }
  }


}
