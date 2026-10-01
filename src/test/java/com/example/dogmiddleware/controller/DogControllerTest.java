package com.example.dogmiddleware.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.dogmiddleware.constant.ErrorMessages;
import com.example.dogmiddleware.constant.HeaderConstants;
import com.example.dogmiddleware.dto.BreedListData;
import com.example.dogmiddleware.exception.ResourceNotFoundException;
import com.example.dogmiddleware.service.DogService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import com.example.dogmiddleware.exception.ApiExceptionHandler;
import com.example.dogmiddleware.filter.CorrelationIdFilter;

@WebMvcTest(DogController.class)
@Import({ApiExceptionHandler.class, CorrelationIdFilter.class})
class DogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DogService dogService;

    @Test
    void getRandomImage_ShouldReturnEapiResponseAndCorrelationId() throws Exception {
        String imageUrl = "https://images.dog.ceo/breeds/hound-afghan/image.jpg";
        when(dogService.getRandomImage()).thenReturn(imageUrl);

        mockMvc.perform(get("/dogs/random").header(HeaderConstants.CORRELATION_ID, "request-123"))
                .andExpect(status().isOk())
                .andExpect(header().string(HeaderConstants.CORRELATION_ID, "request-123"))
                .andExpect(jsonPath("$.data").value(imageUrl))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void getRandomImageByBreed_ShouldReturnBadRequest_WhenBreedHasInvalidCharacters()
            throws Exception {
        mockMvc.perform(get("/dogs/bad-breed"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.message").value(ErrorMessages.INVALID_BREED_FORMAT))
                .andExpect(jsonPath("$.status").value("FAILED"));

        verifyNoInteractions(dogService);
    }

    @Test
    void getRandomImageByBreed_ShouldReturnNotFound_WhenBreedDoesNotExist() throws Exception {
        when(dogService.getRandomImageByBreed("unknown"))
                .thenThrow(new ResourceNotFoundException(ErrorMessages.BREED_NOT_FOUND));

        mockMvc.perform(get("/dogs/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.message").value(ErrorMessages.BREED_NOT_FOUND))
                .andExpect(jsonPath("$.status").value("FAILED"));
    }

    @Test
    void getRandomImageBySubBreed_ShouldReturnOneImageUrl() throws Exception {
        String imageUrl = "https://images.dog.ceo/breeds/bulldog-english/image.jpg";
        when(dogService.getRandomImageBySubBreed("bulldog", "english")).thenReturn(imageUrl);

        mockMvc.perform(get("/dogs/bulldog/english"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(imageUrl))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void getBreeds_ShouldWrapBreedMapInDataObject() throws Exception {
        when(dogService.getBreeds()).thenReturn(
                new BreedListData(Map.of("african", List.of("wild"), "akita", List.of())));

        mockMvc.perform(get("/breeds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.breeds.african[0]").value("wild"))
                .andExpect(jsonPath("$.data.breeds.akita").isArray())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
}
