package com.chickentest.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class OpenAiClient {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String openAiUrl;

    public OpenAiClient(ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    public String generateReport(String prompt) {
        try {
        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put("model", "gpt-3.5-turbo");
        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode userMsg = objectMapper.createObjectNode();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);
        messages.add(userMsg);
        rootNode.set("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(rootNode), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(openAiUrl, request, String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("OpenAI API error: " + response.getStatusCode() + " - " + response.getBody());
        }

        JsonNode json = objectMapper.readTree(response.getBody());
        JsonNode choices = json.path("choices");
        if (choices.isMissingNode() || !choices.isArray() || choices.size() == 0) {
            throw new RuntimeException("OpenAI API: Unexpected response: " + response.getBody());
        }
        return choices.get(0).path("message").path("content").asText();
    } catch (Exception e) {
        throw new RuntimeException("Error communicating with OpenAI API: " + e.getMessage(), e);
    }
    }
}
