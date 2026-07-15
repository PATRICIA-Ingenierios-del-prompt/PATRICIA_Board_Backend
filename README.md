# Board Backend Microservice

Este microservicio es responsable de gestionar el estado y la comunicación en tiempo real de las pizarras colaborativas ("whiteboards"). Forma parte del ecosistema y se encarga de habilitar la colaboración sincronizada entre múltiples usuarios que editan el mismo tablero simultáneamente.

## ¿Qué hace el microservicio?

1. **Gestión en Tiempo Real:** Utiliza WebSockets (STOMP) para enviar y recibir trazos (`strokes`) y posiciones de cursores de los usuarios en tiempo real.
2. **Sincronización Multi-nodo (Backplane):** Implementa un patrón de *Backplane* usando Redis Pub/Sub. Esto permite que el servicio escale a múltiples instancias (pods). Si dos usuarios están conectados a distintas instancias de la aplicación pero en el mismo tablero, Redis se encarga de rutear los eventos de WebSocket entre las instancias.
3. **Persistencia de Estado:** Almacena la verdad absoluta del tablero en MongoDB. A diferencia de un simple *broadcast*, cualquier trazo nuevo se consolida en la base de datos para que los nuevos participantes puedan cargar el tablero completo al unirse.
4. **Integración Orientada a Eventos:** Escucha eventos de dominio a través de RabbitMQ (AMQP). Por ejemplo, al recibir el evento `parche.created`, el microservicio provisiona una nueva pizarra de manera asíncrona y emite el evento de integración `parche.board.ready`.

---

## Parámetros de Calidad y Principios de Diseño

El proyecto ha sido diseñado siguiendo estándares de alta calidad y buenas prácticas:

* **Principios SOLID:**
  * *Single Responsibility Principle (SRP):* Separación clara entre Controladores REST (`BoardController`), WebSockets (`BoardWebSocketController`), Lógica de Negocio (`BoardService`), Mensajería asíncrona (`ParcheCreatedListener`) y comunicación inter-pod (`RedisBackplanePublisher`).
  * *Dependency Inversion Principle (DIP):* Uso de inyección de dependencias a lo largo del framework de Spring Boot, desacoplando los componentes a través de interfaces implícitas y constructores inyectados.
* **Alta Disponibilidad y Escalabilidad Horizontal:** Diseñado sin estado ("stateless") en la capa de aplicación, delegando el estado efímero en memoria al Redis Backplane y la persistencia a MongoDB.
* **Tolerancia a Fallos:** Configuración de *Health Probes* (liveness, readiness) a través de Spring Boot Actuator para integrarse perfectamente con los mecanismos de resiliencia de Kubernetes.
* **Testing y Code Coverage:** Se impone un *Coverage Gate* utilizando JaCoCo (mínimo 60% en líneas, 50% en branches) en el pipeline de CI/CD para garantizar que el core de la lógica de negocio esté bien probado.

---

## Diagrama de Arquitectura

El siguiente diagrama detalla cómo interactúan los componentes lógicos del microservicio con el exterior:

```mermaid
flowchart TD
    Client[Cliente Web / Móvil]

    subgraph "Board Backend (Spring Boot)"
        REST[REST Controller]
        WS[WebSocket Controller]
        Service[Board Service]
        Backplane[Redis Backplane Relay]
        AMQP[RabbitMQ Listener / Publisher]
    end

    Redis[(Redis Cluster\nPub/Sub)]
    Mongo[(MongoDB\nState Storage)]
    RabbitMQ[RabbitMQ\nEvent Broker]

    %% Client Interactions
    Client -- HTTP GET/POST --> REST
    Client -- WS/STOMP --> WS

    %% Internal flow
    REST --> Service
    WS --> Service
    WS -. Inter-Pod sync .-> Backplane

    %% Persistence
    Service --> Mongo

    %% Redis Backplane
    Backplane -- Pub/Sub --> Redis

    %% Event Broker
    RabbitMQ -- parche.created --> AMQP
    AMQP -- provision --> Service
    AMQP -- parche.board.ready --> RabbitMQ
```

---

## Diagrama de Despliegue

El despliegue está diseñado para infraestructuras *Cloud Native* utilizando Kubernetes.

```mermaid
flowchart TB
    Internet((Internet))
    LB[Load Balancer / Ingress]

    subgraph "Kubernetes Cluster"
        subgraph "Board Backend Deployment"
            Pod1[Board Pod 1]
            Pod2[Board Pod 2]
            PodN[Board Pod N]
        end
    end

    Internet --> LB
    LB --> Pod1
    LB --> Pod2
    LB --> PodN

    subgraph "Managed Services / Infraestructura Externa"
        MongoDBAtlas[(MongoDB Atlas)]
        ManagedRedis[(Managed Redis)]
        ManagedRabbit[(Managed RabbitMQ)]
    end

    Pod1 & Pod2 & PodN --> MongoDBAtlas
    Pod1 & Pod2 & PodN --> ManagedRedis
    Pod1 & Pod2 & PodN --> ManagedRabbit
```

## Tecnologías Principales

* Java 21
* Spring Boot 3.2.4
* Spring Web, Spring WebSockets
* Spring Data MongoDB
* Spring AMQP (RabbitMQ)
* Spring Data Redis (Backplane)
* Reactor Netty
* JaCoCo (Coverage)
* OpenAPI / Swagger UI

## API Documentation

The service exposes a RESTful API documented via OpenAPI. Once the application is running, you can explore the API using the Swagger UI available at:
```
http://<HOST>:<PORT>/swagger-ui.html
```
The OpenAPI specification is generated automatically by Springdoc and can be accessed at `/v3/api-docs`.

## Running Locally

### Prerequisites
- Java 21 (or newer)
- Maven 3.9+
- Docker (optional, for containerized execution)
- Access to a MongoDB instance (local or remote)
- Access to a Redis instance (local or remote)
- Access to a RabbitMQ broker (local or remote)

### Steps
1. Clone the repository and navigate to the project root.
2. Set the required environment variables (see *Configuration* section below).
3. Build the project:
   ```
   ./mvnw clean package
   ```
4. Run the application:
   ```
   java -jar target/board-backend-0.0.1-SNAPSHOT.jar
   ```
   The service will start on port **8080** by default.

## Docker Deployment

A Dockerfile is provided for containerizing the microservice. Build and run the image with:
```bash
docker build -t board-backend:latest .

docker run -d \
  -p 8080:8080 \
  -e "SPRING_PROFILES_ACTIVE=prod" \
  -e "MONGODB_URI=mongodb://mongo:27017/board" \
  -e "SPRING_REDIS_HOST=redis" \
  -e "SPRING_RABBITMQ_HOST=rabbitmq" \
  board-backend:latest
```
The container expects external services (MongoDB, Redis, RabbitMQ) to be reachable via the provided hostnames. When deploying to Kubernetes, use the *Deployment* diagram above and configure the corresponding `ConfigMap`/`Secret` resources.

## Testing

Unit and integration tests are located under `src/test/java`. Run the full test suite with:
```bash
./mvnw verify
```
Coverage is enforced by JaCoCo with a minimum of **60%** line coverage and **50%** branch coverage.

## Contributing

Contributions are welcome! Please follow these steps:
1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/awesome-feature`).
3. Implement your changes, ensuring existing tests pass and adding new tests if needed.
4. Submit a Pull Request with a clear description of the changes.

All contributions must adhere to the project's coding standards and pass the CI pipeline.

## License

This project is licensed under the **Apache License 2.0**. See the `LICENSE` file for details.