package com.example.dogmiddleware.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.example.dogmiddleware.exception.DogApiException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class DogApiClientTest {

    private MockRestServiceServer server;
    private DogApiClient dogApiClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://dog.ceo/api");
        server = MockRestServiceServer.bindTo(builder).build();
        dogApiClient = new DogApiClient(builder.build());
    }

    @Test
    void getRandomImageByBreed_ShouldCallMappedDogApiEndpoint() {
        String imageUrl = "https://images.dog.ceo/breeds/hound-afghan/image.jpg";
        server.expect(requestTo("https://dog.ceo/api/breed/hound/images/random"))
                .andRespond(withSuccess(
                        "{\"message\":\"" + imageUrl + "\",\"status\":\"success\"}",
                        MediaType.APPLICATION_JSON));

        assertEquals(imageUrl, dogApiClient.getRandomImageByBreed("hound"));
        server.verify();
    }

    @Test
    void getRandomImage_ShouldCallMappedDogApiEndpoint() {
        String imageUrl = "https://images.dog.ceo/breeds/hound-afghan/image.jpg";
        server.expect(requestTo("https://dog.ceo/api/breeds/image/random"))
                .andRespond(withSuccess(
                        "{\"message\":\"" + imageUrl + "\",\"status\":\"success\"}",
                        MediaType.APPLICATION_JSON));

        assertEquals(imageUrl, dogApiClient.getRandomImage());
        server.verify();
    }

    @Test
    void getRandomImageBySubBreed_ShouldCallMappedDogApiEndpoint() {
        String imageUrl = "https://images.dog.ceo/breeds/bulldog-english/image.jpg";
        server.expect(requestTo("https://dog.ceo/api/breed/bulldog/english/images/random"))
                .andRespond(withSuccess(
                        "{\"message\":\"" + imageUrl + "\",\"status\":\"success\"}",
                        MediaType.APPLICATION_JSON));

        assertEquals(imageUrl, dogApiClient.getRandomImageBySubBreed("bulldog", "english"));
        server.verify();
    }

    @Test
    void getBreeds_ShouldCallMappedDogApiEndpoint() {
        server.expect(requestTo("https://dog.ceo/api/breeds/list/all"))
                .andRespond(withSuccess(
                        "{\"message\":{\"akita\":[],\"hound\":[\"afghan\"]},\"status\":\"success\"}",
                        MediaType.APPLICATION_JSON));

        assertEquals(Map.of("akita", List.of(), "hound", List.of("afghan")),
                dogApiClient.getBreeds());
        server.verify();
    }

    @Test
    void getSubBreeds_ShouldReturnDownstreamSubBreedList() {
        server.expect(requestTo("https://dog.ceo/api/breed/bulldog/list"))
                .andRespond(withSuccess(
                        "{\"message\":[\"english\",\"french\"],\"status\":\"success\"}",
                        MediaType.APPLICATION_JSON));

        assertEquals(List.of("english", "french"), dogApiClient.getSubBreeds("bulldog"));
        server.verify();
    }

    @Test
    void getRandomImageByBreed_ShouldTranslateDownstreamErrorWithoutExposingBody() {
        server.expect(requestTo("https://dog.ceo/api/breed/unknown/images/random"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .body("{\"message\":\"provider-specific details\",\"status\":\"error\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        DogApiException exception = assertThrows(DogApiException.class,
                () -> dogApiClient.getRandomImageByBreed("unknown"));

        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getStatusCode().value());
        assertEquals("Dog API is unavailable", exception.getMessage());
        server.verify();
    }

    @Test
    void getRandomImage_ShouldRejectMalformedDownstreamImageUrl() {
        server.expect(requestTo("https://dog.ceo/api/breeds/image/random"))
                .andRespond(withSuccess(
                        "{\"message\":\"not-a-url\",\"status\":\"success\"}",
                        MediaType.APPLICATION_JSON));

        DogApiException exception = assertThrows(DogApiException.class,
                () -> dogApiClient.getRandomImage());

        assertEquals(HttpStatus.BAD_GATEWAY.value(), exception.getStatusCode().value());
        server.verify();
    }

    @Test
    void getRandomImage_ShouldTranslateMalformedDownstreamJson() {
        server.expect(requestTo("https://dog.ceo/api/breeds/image/random"))
                .andRespond(withSuccess("{malformed-json", MediaType.APPLICATION_JSON));

        DogApiException exception = assertThrows(DogApiException.class,
                () -> dogApiClient.getRandomImage());

        assertEquals(HttpStatus.BAD_GATEWAY.value(), exception.getStatusCode().value());
        server.verify();
    }
}
