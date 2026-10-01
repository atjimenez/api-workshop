package com.example.dogmiddleware.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.dogmiddleware.constant.ErrorMessages;
import com.example.dogmiddleware.exception.DogApiException;
import com.example.dogmiddleware.exception.ResourceNotFoundException;
import com.example.dogmiddleware.integration.DogApiClient;
import com.example.dogmiddleware.dto.BreedListData;
import java.util.Map;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class DogServiceTest {

    @Mock
    private DogApiClient dogApiClient;

    private DogService dogService;

    @BeforeEach
    void setUp() {
        dogService = new DogService(dogApiClient);
    }

    @Test
    void getRandomImage_ShouldReturnImageUrl_WhenDogApiSucceeds() {
        String imageUrl = "https://images.dog.ceo/breeds/hound-afghan/image.jpg";
        when(dogApiClient.getRandomImage()).thenReturn(imageUrl);

        assertEquals(imageUrl, dogService.getRandomImage());
        verify(dogApiClient).getRandomImage();
    }

    @Test
    void getRandomImageByBreed_ShouldNormalizeBreed_WhenInputUsesUppercase() {
        String imageUrl = "https://images.dog.ceo/breeds/hound-afghan/image.jpg";
        when(dogApiClient.getRandomImageByBreed("hound")).thenReturn(imageUrl);

        assertEquals(imageUrl, dogService.getRandomImageByBreed("HOUND"));
        verify(dogApiClient).getRandomImageByBreed("hound");
    }

    @Test
    void getRandomImageByBreed_ShouldThrowNotFound_WhenBreedDoesNotExist() {
        when(dogApiClient.getRandomImageByBreed("unknown"))
                .thenThrow(new DogApiException(HttpStatus.NOT_FOUND, "Downstream 404", null));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> dogService.getRandomImageByBreed("unknown"));

        assertEquals(ErrorMessages.BREED_NOT_FOUND, exception.getMessage());
    }

    @Test
    void getRandomImageBySubBreed_ShouldReturnImage_WhenSubBreedBelongsToBreed() {
        String imageUrl = "https://images.dog.ceo/breeds/bulldog-english/image.jpg";
        when(dogApiClient.getSubBreeds("bulldog")).thenReturn(List.of("english", "french"));
        when(dogApiClient.getRandomImageBySubBreed("bulldog", "english")).thenReturn(imageUrl);

        assertEquals(imageUrl, dogService.getRandomImageBySubBreed("BULLDOG", "ENGLISH"));
        verify(dogApiClient).getSubBreeds("bulldog");
        verify(dogApiClient).getRandomImageBySubBreed("bulldog", "english");
    }

    @Test
    void getRandomImageBySubBreed_ShouldThrowNotFound_WhenSubBreedIsNotUnderBreed() {
        when(dogApiClient.getSubBreeds("bulldog")).thenReturn(List.of("english"));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> dogService.getRandomImageBySubBreed("bulldog", "afghan"));

        assertEquals(ErrorMessages.SUB_BREED_NOT_FOUND, exception.getMessage());
    }

    @Test
    void getRandomImageBySubBreed_ShouldThrowBreedNotFound_WhenParentBreedDoesNotExist() {
        when(dogApiClient.getSubBreeds("unknown"))
                .thenThrow(new DogApiException(HttpStatus.NOT_FOUND, "Downstream 404", null));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> dogService.getRandomImageBySubBreed("unknown", "sub"));

        assertEquals(ErrorMessages.BREED_NOT_FOUND, exception.getMessage());
    }

    @Test
    void getRandomImageByBreed_ShouldPropagateDogApiException_WhenDownstreamErrorIsNotNotFound() {
        DogApiException dogApiException = new DogApiException(
                HttpStatus.BAD_GATEWAY,
                ErrorMessages.DOG_API_UNAVAILABLE,
                null);

        when(dogApiClient.getRandomImageByBreed("hound"))
                .thenThrow(dogApiException);

        DogApiException exception = assertThrows(
                DogApiException.class,
                () -> dogService.getRandomImageByBreed("hound"));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
    }

    @Test
    void getRandomImageBySubBreed_ShouldPropagateDogApiException_WhenSubBreedLookupFails() {
        DogApiException dogApiException = new DogApiException(
                HttpStatus.BAD_GATEWAY,
                ErrorMessages.DOG_API_UNAVAILABLE,
                null);

        when(dogApiClient.getSubBreeds("bulldog"))
                .thenThrow(dogApiException);

        DogApiException exception = assertThrows(
                DogApiException.class,
                () -> dogService.getRandomImageBySubBreed("bulldog", "english"));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
    }

    @Test
    void getRandomImageBySubBreed_ShouldThrowSubBreedNotFound_WhenImageRequestReturnsNotFound() {
        when(dogApiClient.getSubBreeds("bulldog"))
                .thenReturn(List.of("english"));

        when(dogApiClient.getRandomImageBySubBreed("bulldog", "english"))
                .thenThrow(new DogApiException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.SUB_BREED_NOT_FOUND,
                        null));

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> dogService.getRandomImageBySubBreed(
                        "bulldog",
                        "english"));

        assertEquals(
                ErrorMessages.SUB_BREED_NOT_FOUND,
                exception.getMessage());
    }

    @Test
    void getRandomImageBySubBreed_ShouldPropagateDogApiException_WhenImageRequestFails() {
        when(dogApiClient.getSubBreeds("bulldog"))
                .thenReturn(List.of("english"));

        DogApiException dogApiException = new DogApiException(
                HttpStatus.BAD_GATEWAY,
                ErrorMessages.DOG_API_UNAVAILABLE,
                null);

        when(dogApiClient.getRandomImageBySubBreed("bulldog", "english"))
                .thenThrow(dogApiException);

        DogApiException exception = assertThrows(
                DogApiException.class,
                () -> dogService.getRandomImageBySubBreed(
                        "bulldog",
                        "english"));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
    }

    @Test
    void getBreeds_ShouldReturnBreedListData_WhenDogApiSucceeds() {
        Map<String, List<String>> breeds = Map.of(
                "bulldog", List.of("english", "french"),
                "hound", List.of("afghan"));

        when(dogApiClient.getBreeds()).thenReturn(breeds);

        BreedListData result = dogService.getBreeds();

        assertEquals(breeds, result.breeds());
        verify(dogApiClient).getBreeds();
    }
}
