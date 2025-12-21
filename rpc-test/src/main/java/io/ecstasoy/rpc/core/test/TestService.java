package io.ecstasoy.rpc.core.test;

import io.ecstasoy.rpc.common.circuit.CircuitBreaker;
import org.springframework.stereotype.Service;

@Service
public interface TestService {

  String echo(String message);

  CircuitBreaker getCircuitBreaker();
}
