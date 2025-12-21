package io.ecstasoy.rpc.common.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for POST requests.
 *
 * <p>Used to mark a method as a POST request.
 *
 * @author Kunhua Huang
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface POST {

    /**
     * Path of the POST request.
     *
     * @return path of the POST request
     */
    String value() default "";
}
