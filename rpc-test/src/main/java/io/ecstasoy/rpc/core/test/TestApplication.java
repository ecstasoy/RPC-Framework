package io.ecstasoy.rpc.core.test;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = {
    "org.example.rpc.core"
})
public class TestApplication {
}
