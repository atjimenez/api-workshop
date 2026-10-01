package com.example.dogmiddleware.integration;

import com.example.dogmiddleware.constant.ErrorMessages;
import com.example.dogmiddleware.exception.DogApiException;
import com.example.dogmiddleware.integration.dto.DogApiResponse;
import java.net.URI;
import java.net.http.HttpTimeoutException;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class DogApiClient {

    private static final ParameterizedTypeReference<DogApiResponse<String>> STRING_RESPONSE = new ParameterizedTypeReference<>() {
    };
    private static final ParameterizedTypeReference<DogApiResponse<Map<String, List<String>>>> BREEDS_RESPONSE = new ParameterizedTypeReference<>() {
    };
    private static final ParameterizedTypeReference<DogApiResponse<List<String>>> SUB_BREEDS_RESPONSE = new ParameterizedTypeReference<>() {
    };

    private final RestClient restClient;

    public DogApiClient(RestClient dogApiRestClient) {
        this.restClient = dogApiRestClient;
    }

    public String getRandomImage() {
        return getImage("/breeds/image/random", STRING_RESPONSE);
    }

    public String getRandomImageByBreed(String breed) {
        return getImage("/breed/{breed}/images/random", STRING_RESPONSE, breed);
    }

    public String getRandomImageBySubBreed(String breed, String subBreed) {
        return getImage("/breed/{breed}/{subBreed}/images/random", STRING_RESPONSE, breed, subBreed);
    }

    public Map<String, List<String>> getBreeds() {
        return get("/breeds/list/all", BREEDS_RESPONSE);
    }

    public List<String> getSubBreeds(String breed) {
        return get("/breed/{breed}/list", SUB_BREEDS_RESPONSE, breed);
    }

    private String getImage(String uri, ParameterizedTypeReference<DogApiResponse<String>> responseType,
            Object... uriVariables) {
        String imageUrl = get(uri, responseType, uriVariables);
        try {
            URI parsedUrl = URI.create(imageUrl);
            if (!("https".equalsIgnoreCase(parsedUrl.getScheme())
                    || "http".equalsIgnoreCase(parsedUrl.getScheme()))
                    || parsedUrl.getHost() == null) {
                throw new IllegalArgumentException("Dog API returned an invalid image URL");
            }
        } catch (IllegalArgumentException exception) {
            throw new DogApiException(HttpStatus.BAD_GATEWAY,
                    ErrorMessages.DOG_API_UNAVAILABLE, exception);
        }
        return imageUrl;
    }

    private <T> T get(String uri, ParameterizedTypeReference<DogApiResponse<T>> responseType,
            Object... uriVariables) {

        try {
            if (uri == null || uri.isBlank()) {
                throw new IllegalArgumentException(ErrorMessages.URI_NULL_OR_EMPTY);
            }

            if (uriVariables == null) {
                throw new IllegalArgumentException(ErrorMessages.URIVariables_NULL_OR_EMPTY);
            }

            if (responseType == null) {
                throw new IllegalArgumentException(ErrorMessages.RESPONSE_TYPE_NULL);
            }

            RestClient.RequestHeadersSpec<?> request = uriVariables.length == 0
                    ? restClient.get().uri(uri)
                    : restClient.get().uri(uri, uriVariables);

            DogApiResponse<T> response = request
                    .retrieve()
                    .body(responseType);

            if (response == null
                    || !"success".equalsIgnoreCase(response.status())
                    || response.message() == null) {
                throw new DogApiException(
                        HttpStatus.BAD_GATEWAY,
                        ErrorMessages.DOG_API_UNAVAILABLE,
                        null);
            }

            return response.message();

        } catch (RestClientResponseException exception) {
            HttpStatus status = exception.getStatusCode().value() == HttpStatus.NOT_FOUND.value()
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.BAD_GATEWAY;

            throw new DogApiException(
                    status,
                    ErrorMessages.DOG_API_UNAVAILABLE,
                    exception);

        } catch (ResourceAccessException exception) {
            HttpStatus status = hasTimeoutCause(exception)
                    ? HttpStatus.GATEWAY_TIMEOUT
                    : HttpStatus.BAD_GATEWAY;

            String message = status == HttpStatus.GATEWAY_TIMEOUT
                    ? ErrorMessages.DOG_API_TIMEOUT
                    : ErrorMessages.DOG_API_UNAVAILABLE;

            throw new DogApiException(status, message, exception);

        } catch (RestClientException exception) {
            throw new DogApiException(
                    HttpStatus.BAD_GATEWAY,
                    ErrorMessages.DOG_API_UNAVAILABLE,
                    exception);
        }
    }

    private boolean hasTimeoutCause(Throwable exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof HttpTimeoutException
                    || cause instanceof java.net.SocketTimeoutException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
