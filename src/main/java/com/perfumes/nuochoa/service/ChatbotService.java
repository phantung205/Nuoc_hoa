package com.perfumes.nuochoa.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class ChatbotService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String OLLAMA_URL = "http://localhost:11434/api/chat";

    public String askOllama(String userPrompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "qwen2.5:1.5b");
        requestBody.put("stream", false);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "Bạn là trợ lý tư vấn nước hoa nhiệt tình và thân thiện."),
                Map.of("role", "user", "content", userPrompt)
        ));

        try {
            Map response = restTemplate.postForObject(OLLAMA_URL, requestBody, Map.class);
            if (response != null && response.containsKey("message")) {
                Map message = (Map) response.get("message");
                return (String) message.get("content");
            }
        } catch (Exception e) {
            return "Rất tiếc, AI đang không khả dụng.";
        }
        return "Không có phản hồi từ AI.";
    }
}