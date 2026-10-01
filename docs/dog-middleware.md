# Dog Middleware

Spring Boot 3 / Java 21 middleware that exposes the consumer contract documented in
`[ExAPI] DOG API.pdf`, starting at page 3 ("Standard Response Structure"). The
Dog CEO API is used only as a downstream integration; its response envelope is
translated into the EAPI response and is never returned directly.

This application intentionally has no authentication or authorization. The
authentication material before the specified page 3 section is out of scope.

## Run

Build and test with Maven:

```shell
mvn clean verify
```

Run locally with the default `local` profile:

```shell
mvn spring-boot:run
```

Or build and run the executable JAR:

```shell
mvn clean package
java -jar target/dog-middleware-0.0.1-SNAPSHOT.jar
```

Select another environment with either:

```shell
java -jar target/dog-middleware-0.0.1-SNAPSHOT.jar --spring.profiles.active=uat
```

or set `SPRING_PROFILES_ACTIVE=prod` (or `uat`) in the process environment.
`application-local.yml`, `application-uat.yml`, and `application-prod.yml`
contain the profile-specific logging and Swagger settings. Swagger is enabled
locally and disabled by default in UAT and Production; `SWAGGER_ENABLED` can
override the profile default. When enabled, Swagger UI is at `/swagger-ui.html`
and the OpenAPI document is at `/v3/api-docs`.

## Configuration

| Setting | Environment variable | Default |
| --- | --- | --- |
| Application port | `SERVER_PORT` | `8080` |
| Dog API base URL | `DOG_API_BASE_URL` | `https://dog.ceo/api` |
| Connection timeout (ms) | `DOG_API_CONNECT_TIMEOUT` | `5000` |
| Response timeout (ms) | `DOG_API_READ_TIMEOUT` | `10000` |
| Swagger availability | `SWAGGER_ENABLED` | `true` locally; `false` in UAT/Production |

The Dog API base URL and timeout settings use validated
`external.dog-api` configuration properties. The external URL can be changed
without changing any consumer-facing route.

## Consumer API

Every response uses:

```json
{
  "data": {},
  "message": null,
  "status": "SUCCESS"
}
```

Failures use the same fields, `data: null`, and `status: "FAILED"`. HTTP status
codes also indicate the outcome.

| Internal endpoint | Downstream Dog API endpoint | Success payload |
| --- | --- | --- |
| `GET /dogs/random` | `GET /breeds/image/random` | One image URL string |
| `GET /dogs/{breed}` | `GET /breed/{breed}/images/random` | One image URL string |
| `GET /dogs/{breed}/{subBreed}` | `GET /breed/{breed}/{subBreed}/images/random` | One image URL string |
| `GET /breeds` | `GET /breeds/list/all` | `{ "breeds": { "<breed>": ["<sub-breed>"] } }` |

Breed and sub-breed path segments accept alphabetic keys up to 50 characters
and are normalized to lowercase. The sub-breed endpoint verifies membership in
the specified breed using the Dog CEO API's `GET /breed/{breed}/list` before
retrieving an image.

Expected errors include `400` for malformed path parameters, `404` with
`"Breed not found"` or `"Sub-breed not found"` for unsupported values, `502`
when the downstream API is unavailable, and `504` for downstream timeouts.
Unexpected application errors return a safe `500` response.

Each request gets an `X-Correlation-Id` response header. Valid incoming IDs
(`A-Z`, `a-z`, digits, `.`, `_`, and `-`, up to 128 characters) are preserved;
otherwise a UUID is generated. The ID is placed in MDC for request logging.
The health endpoint is `/actuator/health`.

## Architecture

```text
Consumer -> DogController -> DogService -> DogApiClient (RestClient) -> Dog CEO API
                 |               |                 |
                 +---- ApiResponse wrapper / mapped errors ----+
```

Controllers own HTTP and validation concerns, services normalize and validate
breed relationships, and the integration client owns all downstream calls and
downstream response parsing. No persistence is used.
