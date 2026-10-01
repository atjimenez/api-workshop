package com.example.dogmiddleware.constant;

public final class ErrorMessages {

    public static final String BREED_NOT_FOUND = "Breed not found";
    public static final String SUB_BREED_NOT_FOUND = "Sub-breed not found";
    public static final String INVALID_BREED_FORMAT = "Breed or sub-breed format is invalid";
    public static final String RESOURCE_NOT_FOUND = "Resource not found";
    public static final String METHOD_NOT_ALLOWED = "Method not allowed";
    public static final String DOG_API_UNAVAILABLE = "Dog API is unavailable";
    public static final String DOG_API_TIMEOUT = "Dog API request timed out";
    public static final String INTERNAL_SERVER_ERROR = "Internal server error";
    public static final String MALFORMED_BREED = "Malformed breed";
    public static final String MALFORMED_BREED_SUBBREED = "Malformed breed or sub-breed";
    public static final String BREED_SUBBREED_NOT_FOUND = "Breed or sub-breed not found";
    public static final String URI_NULL_OR_EMPTY = "URI must not be null or empty";
    public static final String URIVariables_NULL_OR_EMPTY = "URI variables must not be null or empty";
    public static final String RESPONSE_TYPE_NULL = "Response type must not be null";

    private ErrorMessages() {
    }
}