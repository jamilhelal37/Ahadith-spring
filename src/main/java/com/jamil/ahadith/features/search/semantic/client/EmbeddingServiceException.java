package com.jamil.ahadith.features.search.semantic.client;

public class EmbeddingServiceException extends RuntimeException {
    public EmbeddingServiceException(String message) {
        super(message);
    }

    public EmbeddingServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
