package org.example.rpc.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Registry service application.
 *
 * <p>This is the entry point of the registry service.
 * It starts the Spring Boot application and enables component scanning.
 *
 * @author Kunhua Huang
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "org.example.rpc.**"
})
public class RegistryServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(RegistryServiceApplication.class, args);
  }
}