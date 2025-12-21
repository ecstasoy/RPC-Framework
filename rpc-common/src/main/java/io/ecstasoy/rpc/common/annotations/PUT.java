package io.ecstasoy.rpc.common.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for PUT requests.
 *
 * <p>Used to mark a method as a PUT request.
 *
 * @author Kunhua Huang
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PUT {

    /**
     * Path of the PUT request.
     *
     * @return path of the PUT request
     */
    String value() default "";
}