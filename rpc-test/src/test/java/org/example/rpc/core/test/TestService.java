package org.example.rpc.core.test;

import org.example.rpc.common.circuit.CircuitBreaker;

public interface TestService {

  String echo(String message);

  CircuitBreaker getCircuitBreaker();
}
