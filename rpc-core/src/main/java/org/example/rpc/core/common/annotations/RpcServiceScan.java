package org.example.rpc.core.common.annotations;

import org.example.rpc.core.spring.RpcBeanRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Annotation for scanning RPC services.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Import(RpcBeanRegistrar.class)
@Documented
public @interface RpcServiceScan {

  /**
   * Base packages.
   *
   * @return base packages
   */
  String[] basePackages() default {};
}
