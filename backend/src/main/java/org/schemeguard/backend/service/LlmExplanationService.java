package org.schemeguard.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

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

        requestFactory.setReadTimeout(Duration.ofSeconds(180));

        this.restClient = RestClient.builder()
                .baseUrl(ollamaUrl)
                .requestFactory(requestFactory)
                .build();

        this.model = model;
    }

    public String explain(String transactionData) {
        String systemPrompt = """
                Ești asistentul AI al aplicației SchemeGuard AI.

                Răspunde numai în limba română.
                Folosește exclusiv datele primite.
                Nu inventa valori.
                Nu calcula alte comisioane sau rate.
                Nu afișa raționamentul intern.
                Nu explica pașii de gândire.
                Returnează doar răspunsul final.
                Folosește maximum 3 propoziții.
                """;

        String userPrompt = """
                /no_think

                Explică pe scurt rezultatul tranzacției:

                %s

                Menționează statusul, motivul calificării sau necalificării
                și comisionul disponibil.
                Returnează doar răspunsul final în limba română.
                """.formatted(transactionData);

        OllamaRequest request = new OllamaRequest(
                model,
                List.of(
                        new OllamaMessage("system", systemPrompt),
                        new OllamaMessage("user", userPrompt)
                ),
                false,
                false,
                Map.of(
                        "temperature", 0.1,
                        "num_predict", 120,
                        "num_ctx", 2048
                )
        );

        OllamaResponse response = restClient
                .post()
                .uri("/api/chat")
                .body(request)
                .retrieve()
                .body(OllamaResponse.class);

        if (response == null
                || response.message() == null
                || response.message().content() == null) {
            throw new IllegalStateException(
                    "Ollama did not return an explanation"
            );
        }

        String llmContent = response.message().content();
        String cleanedContent = cleanResponse(llmContent);

        if (isInternalReasoning(cleanedContent)) {
            return buildSafeExplanation(transactionData);
        }

        return cleanedContent;
    }

    private String cleanResponse(String content) {
        String result = content;

        int thinkEnd = result.lastIndexOf("</think>");

        if (thinkEnd >= 0) {
            result = result.substring(
                    thinkEnd + "</think>".length()
            );
        }

        int finalMarker = result.lastIndexOf("FINAL:");

        if (finalMarker >= 0) {
            result = result.substring(
                    finalMarker + "FINAL:".length()
            );
        }

        return result
                .replace("<think>", "")
                .replace("</think>", "")
                .trim();
    }

    private boolean isInternalReasoning(String content) {
        String lowerCaseContent = content.toLowerCase();

        return lowerCaseContent.contains("okay, let's")
                || lowerCaseContent.contains("let me")
                || lowerCaseContent.contains("the user wants")
                || lowerCaseContent.contains("i need to")
                || lowerCaseContent.contains("first, i")
                || lowerCaseContent.contains("hmm")
                || lowerCaseContent.contains("the response should");
    }

    private String buildSafeExplanation(String transactionData) {
        String status = extractValue(
                transactionData,
                "Qualification status:"
        );

        String reason = extractValue(
                transactionData,
                "Original explanation:"
        );

        String fee = extractValue(
                transactionData,
                "Interchange fee:"
        );

        if (fee.isBlank()) {
            fee = extractValue(
                    transactionData,
                    "Estimated fee:"
            );
        }

        if (status.isBlank()) {
            status = "necunoscut";
        }

        if (reason.isBlank()) {
            reason = "nu există o explicație suplimentară";
        }

        if (fee.isBlank()
                || fee.equalsIgnoreCase("not available")) {
            return "Tranzacția are statusul "
                    + status
                    + ". Motivul rezultatului este: "
                    + reason
                    + ".";
        }

        return "Tranzacția are statusul "
                + status
                + ". Motivul rezultatului este: "
                + reason
                + ". Comisionul calculat este "
                + fee
                + ".";
    }

    private String extractValue(
            String data,
            String fieldName
    ) {
        int startIndex = data.indexOf(fieldName);

        if (startIndex < 0) {
            return "";
        }

        int valueStart = startIndex + fieldName.length();
        int valueEnd = data.indexOf("\n", valueStart);

        if (valueEnd < 0) {
            valueEnd = data.length();
        }

        return data
                .substring(valueStart, valueEnd)
                .trim();
    }

    private record OllamaRequest(
            String model,
            List<OllamaMessage> messages,
            boolean stream,
            boolean think,
            Map<String, Object> options
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