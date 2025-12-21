package io.ecstasoy.rpc.common.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Body annotation.
 *
 * <p>It is used to mark a parameter as the body of a request.
 *
 * @author Kunhua Huang
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Body {
}