# Dog Middleware

Spring Boot 3 / Java 21 middleware that exposes a consumer-friendly API for the Dog CEO API.

This project acts as a translation layer between clients and the upstream Dog CEO service. It normalizes downstream responses, validates inputs, adds correlation IDs, and returns a consistent response wrapper.

## Features

- Random dog image endpoint
- Breed-based random image endpoint
- Sub-breed random image endpoint
- Breed listing endpoint
- Standardized `ApiResponse` envelope
- Request correlation IDs and health endpoint
- OpenAPI/Swagger documentation

## Run locally

```bash
mvn clean verify
mvn spring-boot:run
```

Or build and run the packaged JAR:

```bash
mvn clean package
java -jar target/dog-middleware-0.0.1-SNAPSHOT.jar
```

## API overview

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/dogs/random` | Returns a random dog image URL |
| GET | `/dogs/{breed}` | Returns a random dog image for a breed |
| GET | `/dogs/{breed}/{subBreed}` | Returns a random dog image for a breed and sub-breed |
| GET | `/breeds` | Returns all available breeds and sub-breeds |

## Configuration

The app uses validated configuration properties under `external.dog-api`.

| Setting | Environment variable | Default |
| --- | --- | --- |
| Dog API base URL | `DOG_API_BASE_URL` | `https://dog.ceo/api` |
| Connection timeout (ms) | `DOG_API_CONNECT_TIMEOUT` | `5000` |
| Read timeout (ms) | `DOG_API_READ_TIMEOUT` | `10000` |
| Server port | `SERVER_PORT` | `8081` |

## Response format

All successful and failed responses use the same wrapper:

```json
{
  "data": {},
  "message": null,
  "status": "SUCCESS"
}
```

Errors return the same structure with `data: null` and `status: "FAILED"`, while HTTP status codes indicate the protocol-level outcome.

## Health and docs

- Health endpoint: `/actuator/health`
- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/v3/api-docs`

## Architecture

```text
Consumer -> DogController -> DogService -> DogApiClient -> Dog CEO API
```

The controller layer handles HTTP and validation, the service layer normalizes business rules, and the integration client owns all downstream calls.
