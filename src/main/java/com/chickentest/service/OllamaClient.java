package com.chickentest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class OllamaClient {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ollama.api.url:http://localhost:11434/api/generate}")
    private String ollamaUrl;

    public OllamaClient(ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    public String generateReport(String prompt) {
        String requestBodyJson = String.format(
            "{\"model\": \"llama3\", \"prompt\": \"%s\", \"stream\": false}",
            prompt.replace("\"", "\\\"").replace("\n", "\\n")
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBodyJson, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(ollamaUrl, requestEntity, String.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                String generatedText = rootNode.path("response").asText();
                if (generatedText.isEmpty() && !rootNode.path("error").isMissingNode()) {
                    String errorMessage = rootNode.path("error").asText("Unknown error from AI model.");
                    log.error("Ollama API returned an error in JSON response: {}", errorMessage);
                    throw new RuntimeException("AI model error: " + errorMessage);
                }
                if (generatedText.isEmpty()) {
                    log.warn("Ollama API returned an empty 'response' field.");
                    throw new RuntimeException("AI model returned an empty report.");
                }
                log.info("Successfully generated AI report.");
                return generatedText;
            } else {
                log.error("Ollama API request failed with status: {} and body: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("Error from AI service: " + response.getBody());
            }
        } catch (ResourceAccessException e) { 
            log.error("Error connecting to Ollama service at {}: {}", ollamaUrl, e.getMessage());
            throw new RuntimeException("AI service is currently unavailable. Please try again later.", e);
        } catch (JsonProcessingException e) { 
            log.error("Error parsing Ollama response: {}", e.getMessage(), e);
            throw new RuntimeException("Error parsing AI model's response.", e);
        } catch (RuntimeException e) { 
            log.error("RuntimeException during AI report generation: {}", e.getMessage(), e);
            throw e; 
        }
         catch (Exception e) { 
            log.error("Unexpected exception during AI report generation: {}", e.getMessage(), e);
            throw new RuntimeException("An unexpected error occurred while communicating with the AI model.", e);
        }
    }
}
