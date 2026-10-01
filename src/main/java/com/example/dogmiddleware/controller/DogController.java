package com.example.dogmiddleware.controller;

import com.example.dogmiddleware.constant.ApiConstants;
import com.example.dogmiddleware.constant.ErrorMessages;
import com.example.dogmiddleware.constant.SuccessMessages;
import com.example.dogmiddleware.dto.ApiResponse;
import com.example.dogmiddleware.dto.BreedListData;
import com.example.dogmiddleware.service.DogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@Tag(name = "Dogs", description = "Consumer operations for dog images and breeds")
public class DogController {

        private static final String PATH_SEGMENT_PATTERN = "^[A-Za-z]+$";

        private final DogService dogService;

        public DogController(DogService dogService) {
                this.dogService = dogService;
        }

        @GetMapping(ApiConstants.DOGS_PATH + "/random")
        @Operation(summary = "Retrieve a random dog image", description = "Returns one random dog image URL.")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = SuccessMessages.RANDOM_IMG_RETURNED, content = @Content(mediaType = ApiConstants.MEDIA_TYPE, schema = @Schema(implementation = ApiResponse.class)))
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = ErrorMessages.DOG_API_UNAVAILABLE, content = @Content(mediaType = ApiConstants.MEDIA_TYPE, schema = @Schema(implementation = ApiResponse.class)))
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "504", description = ErrorMessages.DOG_API_TIMEOUT, content = @Content(mediaType = ApiConstants.MEDIA_TYPE, schema = @Schema(implementation = ApiResponse.class)))
        public ResponseEntity<ApiResponse<String>> getRandomImage() {
                return ResponseEntity.ok(ApiResponse.success(dogService.getRandomImage()));
        }

        @GetMapping(ApiConstants.DOGS_PATH + "/{breed}")
        @Operation(summary = "Retrieve a random image by breed", description = "Returns one random image URL for an available breed.")
        @Parameter(name = "breed", in = ParameterIn.PATH, required = true, description = "Breed key; input is case-insensitive.")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = SuccessMessages.RANDOM_IMG_RETURNED, content = @Content(mediaType = ApiConstants.MEDIA_TYPE, schema = @Schema(implementation = ApiResponse.class)))
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = ErrorMessages.MALFORMED_BREED, content = @Content(mediaType = ApiConstants.MEDIA_TYPE, schema = @Schema(implementation = ApiResponse.class)))
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = ErrorMessages.BREED_NOT_FOUND, content = @Content(mediaType = ApiConstants.MEDIA_TYPE, schema = @Schema(implementation = ApiResponse.class)))
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = ErrorMessages.DOG_API_UNAVAILABLE, content = @Content(mediaType = ApiConstants.MEDIA_TYPE, schema = @Schema(implementation = ApiResponse.class)))
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "504", description = ErrorMessages.DOG_API_TIMEOUT, content = @Content(mediaType = ApiConstants.MEDIA_TYPE, schema = @Schema(implementation = ApiResponse.class)))
        public ResponseEntity<ApiResponse<String>> getRandomImageByBreed(
                        @PathVariable @NotBlank(message = ErrorMessages.INVALID_BREED_FORMAT) @Pattern(regexp = PATH_SEGMENT_PATTERN, message = ErrorMessages.INVALID_BREED_FORMAT) @Size(max = 50, message = ErrorMessages.INVALID_BREED_FORMAT) String breed) {
                return ResponseEntity.ok(ApiResponse.success(dogService.getRandomImageByBreed(breed)));
        }

        @GetMapping(ApiConstants.DOGS_PATH + "/{breed}/{subBreed}")
        @Operation(summary = "Retrieve a random image by breed and sub-breed", description = "Returns one random image URL when the sub-breed belongs to the breed.")
        @Parameter(name = "breed", in = ParameterIn.PATH, required = true, description = "Breed key; input is case-insensitive.")
        @Parameter(name = "subBreed", in = ParameterIn.PATH, required = true, description = "Sub-breed key; input is case-insensitive.")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = SuccessMessages.RANDOM_SUBBREED_IMG_RETURNED, content = @Content(mediaType = ApiConstants.MEDIA_TYPE, schema = @Schema(implementation = ApiResponse.class)))
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = ErrorMessages.MALFORMED_BREED_SUBBREED, content = @Content(mediaType = ApiConstants.MEDIA_TYPE, schema = @Schema(implementation = ApiResponse.class)))
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = ErrorMessages.BREED_SUBBREED_NOT_FOUND, content = @Content(mediaType = ApiConstants.MEDIA_TYPE, schema = @Schema(implementation = ApiResponse.class)))
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = ErrorMessages.DOG_API_UNAVAILABLE, content = @Content(mediaType = ApiConstants.MEDIA_TYPE, schema = @Schema(implementation = ApiResponse.class)))
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "504", description = ErrorMessages.DOG_API_TIMEOUT, content = @Content(mediaType = ApiConstants.MEDIA_TYPE, schema = @Schema(implementation = ApiResponse.class)))
        public ResponseEntity<ApiResponse<String>> getRandomImageBySubBreed(
                        @PathVariable @NotBlank(message = ErrorMessages.INVALID_BREED_FORMAT) @Pattern(regexp = PATH_SEGMENT_PATTERN, message = ErrorMessages.INVALID_BREED_FORMAT) @Size(max = 50, message = ErrorMessages.INVALID_BREED_FORMAT) String breed,
                        @PathVariable @NotBlank(message = ErrorMessages.INVALID_BREED_FORMAT) @Pattern(regexp = PATH_SEGMENT_PATTERN, message = ErrorMessages.INVALID_BREED_FORMAT) @Size(max = 50, message = ErrorMessages.INVALID_BREED_FORMAT) String subBreed) {
                return ResponseEntity.ok(ApiResponse.success(
                                dogService.getRandomImageBySubBreed(breed, subBreed)));
        }

        @GetMapping(ApiConstants.BREEDS_PATH)
        @Operation(summary = "Retrieve available breeds and sub-breeds", description = "Returns all breed keys and their associated sub-breed keys.")
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = SuccessMessages.BREED_LIST_RETURNED, content = @Content(schema = @Schema(implementation = ApiResponse.class)))
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = ErrorMessages.DOG_API_UNAVAILABLE, content = @Content(mediaType = ApiConstants.MEDIA_TYPE, schema = @Schema(implementation = ApiResponse.class)))
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "504", description = ErrorMessages.DOG_API_TIMEOUT, content = @Content(mediaType = ApiConstants.MEDIA_TYPE, schema = @Schema(implementation = ApiResponse.class)))
        public ResponseEntity<ApiResponse<BreedListData>> getBreeds() {
                return ResponseEntity.ok(ApiResponse.success(dogService.getBreeds()));
        }
}
