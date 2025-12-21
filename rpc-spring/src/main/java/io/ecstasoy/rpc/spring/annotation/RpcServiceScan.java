package io.ecstasoy.rpc.spring.annotation;

import io.ecstasoy.rpc.spring.RpcBeanRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Annotation for scanning RPC services.
 *
 * <p>Mark the base package with this annotation to scan RPC services.
 *
 * <p>For example:
 * <pre>
 *   {@literal @}RpcServiceScan(basePackages = "io.ecstasoy.rpc")
 *   public class Application {
 *   // ...
 *   }
 *   </pre>
 *
 *  <p>Then the RPC services in the package {@code org.example.rpc} will be scanned and registered.
 *
 * @author Kunhua Huang
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
