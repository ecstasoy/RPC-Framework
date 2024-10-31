package org.example.rpc.common.annotations;

import java.lang.annotation.*;

/**
 * RPC service annotation.
 *
 * <p>Mark the service implementation class with this annotation to expose it as an RPC service.
 *
 * <p>For example:
 * <pre>
 *   {@literal @}RpcService
 *   public class UserServiceImpl implements UserService {
 *   // ...
 *   }
 * </pre>
 *
 * @author Kunhua Huang
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface RpcService {

}
