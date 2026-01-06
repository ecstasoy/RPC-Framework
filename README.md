# RPC Framework
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/189e0d34d5044bd1b553bd6406d95a0b)](https://app.codacy.com/gh/ecstasoy/RPC-Framework/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/ecstasoy/RPC-Framework)

A lightweight, Spring Boot-based RPC (Remote Procedure Call) framework implementation with high performance and extensibility.

## Overview

This project is a distributed RPC framework built on Spring Boot 2.7.3, providing a comprehensive suite of features including service registration, discovery, load balancing, and network transport. The framework utilizes Protocol Buffers for efficient serialization, enabling high-performance remote service invocations.

## Key Features

- Service Registration and Discovery
- Dynamic Service Routing
- Load Balancing
- Request Interception
- Performance Monitoring
- Spring Framework Integration
- Protocol Buffer Serialization
- Extensible Architecture

## System Architecture

The framework adopts a modular design with the following core components:

### Core Components

1. **Registry (rpc-registry)**
   - Service registration and discovery
   - Health checking
   - Service metadata management

2. **Network Transport (rpc-network)**
   - Network communication layer
   - Connection pooling
   - Protocol handling

3. **Protocol (rpc-protocol)**
   - Protocol Buffer message definitions
   - Protocol encoding/decoding
   - Message format specifications

4. **Proxy (rpc-proxy)**
   - Dynamic proxy generation
   - Remote service invocation
   - Local service exposure

5. **Load Balancer (rpc-loadbalancer)**
   - Multiple load balancing strategies
   - Service instance selection
   - Traffic distribution

6. **Router (rpc-router)**
   - Request routing
   - Service versioning
   - Traffic management

7. **Interceptor (rpc-interceptor)**
   - Request/Response interception
   - Cross-cutting concerns
   - Custom business logic injection

8. **Monitor (rpc-monitor)**
   - Performance metrics collection
   - Service health monitoring
   - Statistical analysis

9. **Spring Integration (rpc-spring)**
   - Spring Boot autoconfiguration
   - Annotation support
   - Bean lifecycle management

### Supporting Modules

- **rpc-common**: Shared utilities and common components
- **rpc-handler**: Request processing handlers
- **rpc-processor**: Message processing pipeline
- **rpc-example**: Example implementations and demos
- **rpc-test**: Test cases and integration tests

## Technical Stack

- **Java Version**: 8+
- **Framework**: Spring Boot 2.7.3
- **Serialization**: Protocol Buffers 3.24.0
- **Build Tool**: Maven 3.0+
- **Testing**: JUnit 5, Mockito
- **JSON Processing**: FastJSON, Gson

## Getting Started

### Prerequisites

- JDK 1.8 or higher
- Maven 3.0 or higher
- Git

### Building the Project

1. Clone the repository:
   ```bash
   git clone [repository-url]
   cd rpc-framework
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

### Running the Services

#### Starting Services

1. Start the Registry Server:
   ```bash
   ./start-test-instances.sh
   ```

2. Start Service Providers:
   ```bash
   ./start-blog-instances.sh
   ```

3. Start Service Consumers:
   ```bash
   ./start-user-instances.sh
   ```

#### Stopping Services

Use the corresponding stop scripts:
```bash
./stop-test-instances.sh
./stop-blog-instances.sh
./stop-user-instances.sh
```

### Performance Monitoring

Monitor service performance and health:
```bash
./get-stats.sh
```

## Development Guide

### Project Structure
```
rpc-framework/
├── rpc-common/        # Common utilities
├── rpc-protocol/      # Protocol definitions
├── rpc-network/       # Network layer
├── rpc-registry/      # Service registry
├── rpc-proxy/         # Service proxy
├── rpc-loadbalancer/  # Load balancing
├── rpc-router/        # Request routing
├── rpc-interceptor/   # Interceptors
├── rpc-monitor/       # Monitoring
├── rpc-spring/        # Spring integration
├── rpc-handler/       # Request handlers
├── rpc-processor/     # Message processors
├── rpc-example/       # Examples
└── rpc-test/          # Tests
```

### Coding Standards

- Follow Java coding conventions
- Write comprehensive unit tests
- Document public APIs
- Keep code modular and maintainable

## License

This project is licensed under the MIT License - see the LICENSE file for details.
