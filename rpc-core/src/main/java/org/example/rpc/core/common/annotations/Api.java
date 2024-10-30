package org.example.rpc.core.common.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking a class as an API.
 *
 * <p>It is used to mark a class as an API, which will be scanned by the RPC framework to generate
 *
 * @author Kunhua Huang
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Api {

  /**
   * API value.
   */
  String value() default "";
}