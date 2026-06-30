# FINT FLYT WEB GATEWAY TEMPLATE

## Overview
This template is a skeleton gateway with example code for creating a web instance gateway in FINT Flyt. It receives incoming HTTP requests, maps payloads to `InstanceObject`, and publishes data internally in the FINT Flyt flow.

The project includes two example flows:
- **Simple**: a single instance without collections and without file upload.
- **Advanced**: collections and file upload using the `persistFile` callback.

## Getting Started
Customize this template for your source system before deployment.

### Rename and adjust template identifiers
Update template names and identifiers so they match your source application:
- Rename project/artifact names such as `fint-flyt-example-gateway`.
- Update `fint.application-id` in `src/main/resources/application.yaml`.
- Update metadata and image values in `kustomize/base/flais.yaml`.

### Configure source application access
Set the source application ID received from `fint-flyt-authorization-service`:
- `src/main/resources/application-flyt-web-resource-server.yaml`
- Property: `novari.flyt.web-resource-server.security.api.external.authorized-source-application-ids`

This ensures requests are only accepted for authorized source applications.

### Local application configuration
Adjust local settings in `src/main/resources/application-local-staging.yaml`:
- `server.port`: use `81xx`, where `xx` reflects the source application ID (for example, ID `9` -> port `8109`).

## Processing Instances
Create one `InstanceProcessor` per incoming model type using `InstanceProcessorFactoryService`.

In this template, examples are:
- `src/main/kotlin/no/novari/flyt/example/gateway/instance/simple/SimpleProcessorConfiguration.kt`
- `src/main/kotlin/no/novari/flyt/example/gateway/instance/advanced/AdvancedProcessorConfiguration.kt`

Each processor uses an `InstanceMapper` implementation:
- `src/main/kotlin/no/novari/flyt/example/gateway/instance/simple/SimpleMappingService.kt`
- `src/main/kotlin/no/novari/flyt/example/gateway/instance/advanced/AdvancedMappingService.kt`

Use the advanced mapping example if you need to persist attachments through the file service.

## Testing Locally
Run `docker-compose.yaml` to start local dependencies (Kafka, Kafdrop, Postgres). Kafka messages can be inspected in Kafdrop:
- http://localhost:19000/

Run `fint-flyt-authorization-service` locally as well:
- https://github.com/FINTLabs/fint-flyt-authorization-service
- Configure it with your client ID and client secret.

If authorization service is unavailable, requests will return `403 Forbidden`.
After restarting services, wait 20-30 seconds for local Kafka synchronization before testing requests.

Run this gateway with local profile:
```bash
./gradlew bootRun --args='--spring.profiles.active=local-staging'
```

## Technology
- Spring Boot 3.5.14
- Java 25
- Kotlin 2.4.0
- ktlint
- Blocking architecture (non-reactive)

## Dependencies
This template depends on:
- `no.novari:flyt-web-instance-gateway:1.3.6`

## Additional Documentation
For more details on FINT Flyt architecture and setup, see:
- https://fintlabs.atlassian.net/wiki/spaces/FINTKB/pages/379355169/FINT+Flyt+Arkitektur+event+driven

Document your source application details (data models, request contracts, and endpoints) alongside your implementation.
