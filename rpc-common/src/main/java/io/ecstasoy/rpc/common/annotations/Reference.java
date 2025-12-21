package io.ecstasoy.rpc.common.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Annotation for service reference.
 *
 * <p>Used to replace the @Autowired annotation.
 * <p>Inspired by @Reference in Dubbo.
 *
 * @author Kunhua Huang
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Reference {
}
