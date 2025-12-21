package io.ecstasoy.rpc.common.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for GET requests.
 *
 * <p>It is used to mark a method as a GET request.
 *
 * @author Kunhua Huang
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GET {
    String value() default "";
}
