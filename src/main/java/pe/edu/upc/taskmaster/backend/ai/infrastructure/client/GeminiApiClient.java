package pe.edu.upc.taskmaster.backend.ai.infrastructure.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class GeminiApiClient {

    private final WebClient geminiWebClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.model}")
    private String model;

    @Value("${gemini.api.timeout:30000}")
    private int timeout;

    @Value("${gemini.api.key}")
    private String apiKey;

    public String generateContent(String prompt) {
        try {
            Map<String, Object> request = Map.of(
                    "model", model,
                    "input", prompt
            );

            String response = geminiWebClient
                    .post()
                    .uri("/interactions")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(timeout))
                    .block();

            if (response == null) {
                log.error("Respuesta nula de Gemini");
                return null;
            }

            return extractTextFromResponse(response);

        } catch (Exception e) {
            log.error("Error llamando a Gemini: {}", e.getMessage());
            return null;
        }
    }

    private String extractTextFromResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);

            JsonNode error = root.path("error");
            if (!error.isMissingNode()) {
                String errorMessage = error.path("message").asText();
                log.error("Error de Gemini API: {}", errorMessage);
                return "Error de API: " + errorMessage;
            }

            JsonNode steps = root.path("steps");
            if (steps.isArray() && steps.size() > 0) {
                for (JsonNode step : steps) {
                    String type = step.path("type").asText();
                    if ("model_output".equals(type)) {
                        JsonNode content = step.path("content");
                        if (content.isArray()) {
                            for (JsonNode item : content) {
                                String itemType = item.path("type").asText();
                                if ("text".equals(itemType)) {
                                    String text = item.path("text").asText();
                                    if (text != null && !text.isEmpty()) {
                                        return text;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode text = candidates.get(0)
                        .path("content")
                        .path("parts")
                        .path(0)
                        .path("text");
                if (text != null && !text.isMissingNode()) {
                    return text.asText();
                }
            }

            log.error("No se pudo extraer texto de la respuesta de Gemini");
            return null;

        } catch (Exception e) {
            log.error("Error procesando respuesta de Gemini: {}", e.getMessage());
            return null;
        }
    }
}
