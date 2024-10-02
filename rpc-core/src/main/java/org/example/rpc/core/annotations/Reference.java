package org.example.rpc.core.annotations;

import java.lang.annotation.*;

/**
 * Annotation for service reference.
 * Used to replace the @Autowired annotation.
 * Inspired by @Reference in Dubbo.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Reference {

}
