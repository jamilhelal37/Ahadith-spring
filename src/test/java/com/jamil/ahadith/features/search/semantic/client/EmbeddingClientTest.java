package com.jamil.ahadith.features.search.semantic.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class EmbeddingClientTest {
    @Test
    void parsesAValid1024DimensionResponse() {
        String vector = "[" + String.join(",", java.util.Collections.nCopies(1024, "0.0")) + "]";
        RestClient.Builder builder = RestClient.builder().baseUrl("http://embedding.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(once(), requestTo("http://embedding.test/embed"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"model\":\"BAAI/bge-m3\",\"modelVersion\":\"1.3.5\",\"dimension\":1024,\"embeddings\":[" + vector + "]}", MediaType.APPLICATION_JSON));

        EmbeddingResponse response = new EmbeddingClient(builder.build()).embed(List.of("رحمة"));

        assertThat(response.dimension()).isEqualTo(1024);
        assertThat(response.embeddings().getFirst()).hasSize(1024);
        server.verify();
    }

    @Test
    void convertsHttpFailuresToEmbeddingServiceException() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://embedding.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://embedding.test/embed")).andRespond(withServerError());

        EmbeddingClient client = new EmbeddingClient(builder.build());
        assertThatThrownBy(() -> client.embed(List.of("text")))
                .isInstanceOf(EmbeddingServiceException.class)
                .hasMessageContaining("unavailable");
        server.verify();
    }
}
