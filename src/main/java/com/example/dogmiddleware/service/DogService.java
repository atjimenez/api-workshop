package com.example.dogmiddleware.service;

import com.example.dogmiddleware.constant.ErrorMessages;
import com.example.dogmiddleware.dto.BreedListData;
import com.example.dogmiddleware.exception.DogApiException;
import com.example.dogmiddleware.exception.ResourceNotFoundException;
import com.example.dogmiddleware.integration.DogApiClient;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class DogService {

    private final DogApiClient dogApiClient;

    public DogService(DogApiClient dogApiClient) {
        this.dogApiClient = dogApiClient;
    }

    public String getRandomImage() {
        return dogApiClient.getRandomImage();
    }

    public String getRandomImageByBreed(String breed) {
        String normalizedBreed = normalize(breed);
        try {
            return dogApiClient.getRandomImageByBreed(normalizedBreed);
        } catch (DogApiException exception) {
            if (exception.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                throw new ResourceNotFoundException(ErrorMessages.BREED_NOT_FOUND);
            }
            throw exception;
        }
    }

    public String getRandomImageBySubBreed(String breed, String subBreed) {
        String normalizedBreed = normalize(breed);
        String normalizedSubBreed = normalize(subBreed);
        List<String> subBreeds;
        try {
            subBreeds = dogApiClient.getSubBreeds(normalizedBreed);
        } catch (DogApiException exception) {
            if (exception.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                throw new ResourceNotFoundException(ErrorMessages.BREED_NOT_FOUND);
            }
            throw exception;
        }

        if (!subBreeds.contains(normalizedSubBreed)) {
            throw new ResourceNotFoundException(ErrorMessages.SUB_BREED_NOT_FOUND);
        }

        try {
            return dogApiClient.getRandomImageBySubBreed(normalizedBreed, normalizedSubBreed);
        } catch (DogApiException exception) {
            if (exception.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                throw new ResourceNotFoundException(ErrorMessages.SUB_BREED_NOT_FOUND);
            }
            throw exception;
        }
    }

    public BreedListData getBreeds() {
        Map<String, List<String>> breeds = dogApiClient.getBreeds();
        return new BreedListData(breeds);
    }

    private String normalize(String value) {
        return value.toLowerCase(Locale.ROOT);
    }
}
