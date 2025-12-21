package io.ecstasoy.rpc.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Param annotation.
 *
 * <p>It is used to mark a parameter as a parameter of a request.
 *
 * @author Kunhua Huang
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {

    /**
     * Parameter name.
     *
     * @return parameter name
     */
    String value();
}
