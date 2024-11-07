package org.example.rpc.core.test;

import org.example.rpc.common.circuit.CircuitBreaker;
import org.springframework.stereotype.Service;

@Service
public interface TestService {

  String echo(String message);

  CircuitBreaker getCircuitBreaker();
}
