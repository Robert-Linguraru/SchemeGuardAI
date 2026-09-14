package org.schemeguard.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

@Service
public class LlmExplanationService {

    private final RestClient restClient;
    private final String model;

    public LlmExplanationService(
            @Value("${app.llm.ollama-url}") String ollamaUrl,
            @Value("${app.llm.model}") String model
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);

        requestFactory.setReadTimeout(Duration.ofSeconds(120));

        this.restClient = RestClient.builder()
                .baseUrl(ollamaUrl)
                .requestFactory(requestFactory)
                .build();

        this.model = model;
    }

    public String explain(String transactionData) {
        String systemPrompt = """
                Ești asistentul SchemeGuard AI.
                Explică în limba română rezultatul tranzacției.
                Folosește exclusiv datele primite.
                Nu inventa valori.
                Nu calcula o rată nouă.
                Răspunde în maximum 120 de cuvinte.
                """;

        String userPrompt = """
                Explică următoarea tranzacție:

                %s
                """.formatted(transactionData);

        OllamaResponse response = restClient
                .post()
                .uri("/api/chat")
                .body(new OllamaRequest(
                        model,
                        List.of(
                                new OllamaMessage("system", systemPrompt),
                                new OllamaMessage("user", userPrompt)
                        ),
                        false,
                        false
                ))
                .retrieve()
                .body(OllamaResponse.class);

        if (response == null
                || response.message() == null
                || response.message().content() == null
                || response.message().content().isBlank()) {
            throw new IllegalStateException(
                    "Ollama did not return an explanation"
            );
        }

        return response.message().content().trim();
    }

    private record OllamaRequest(
            String model,
            List<OllamaMessage> messages,
            boolean stream,
            boolean think
    ) {
    }

    private record OllamaMessage(
            String role,
            String content
    ) {
    }

    private record OllamaResponse(
            OllamaMessage message
    ) {
    }
}