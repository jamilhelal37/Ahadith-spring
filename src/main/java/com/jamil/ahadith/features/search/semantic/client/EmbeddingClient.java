package com.jamil.ahadith.features.search.semantic.client;

import com.jamil.ahadith.features.search.semantic.config.SemanticSearchProperties;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Component
public class EmbeddingClient {
    public static final int DIMENSION = 1024;

    private final RestClient restClient;

    public EmbeddingClient(SemanticSearchProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeout());
        requestFactory.setReadTimeout(properties.getReadTimeout());
        this.restClient = RestClient.builder()
                .baseUrl(properties.getServiceUrl())
                .requestFactory(requestFactory)
                .build();
    }

    EmbeddingClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public EmbeddingResponse embed(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            throw new IllegalArgumentException("At least one text is required");
        }
        try {
            EmbeddingResponse response = restClient.post()
                    .uri("/embed")
                    .body(Map.of("texts", texts))
                    .retrieve()
                    .body(EmbeddingResponse.class);
            validate(response, texts.size());
            return response;
        } catch (EmbeddingServiceException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new EmbeddingServiceException("Embedding service is unavailable", ex);
        }
    }

    public EmbeddingHealthResponse health() {
        try {
            EmbeddingHealthResponse response = restClient.get()
                    .uri("/health")
                    .retrieve()
                    .body(EmbeddingHealthResponse.class);
            if (response == null || !"UP".equalsIgnoreCase(response.status()) || response.dimension() != DIMENSION) {
                throw new EmbeddingServiceException("Embedding service returned an invalid health response");
            }
            return response;
        } catch (EmbeddingServiceException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new EmbeddingServiceException("Embedding service is unavailable", ex);
        }
    }

    private void validate(EmbeddingResponse response, int expectedCount) {
        if (response == null || response.dimension() != DIMENSION || response.embeddings() == null
                || response.embeddings().size() != expectedCount
                || response.embeddings().stream().anyMatch(vector -> vector == null || vector.size() != DIMENSION)) {
            throw new EmbeddingServiceException("Embedding service returned an invalid response");
        }
    }
}
