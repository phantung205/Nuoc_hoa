package com.perfumes.nuochoa.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatbotService {

    @Value("${chatbot.ollama.url}")
    private String ollamaApiUrl;

    @Value("${chatbot.ollama.model}")
    private String aiModel;

    @Value("${chatbot.ollama.system-prompt}")
    private String systemPrompt;

    private final RestTemplate restTemplate = new RestTemplate();

    public String askOllama(String userMessage) {
        // Xây dựng request body theo chuẩn Ollama Chat API
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiModel); // Sử dụng biến aiModel thay vì hằng số
        requestBody.put("stream", false);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt), // Sử dụng biến systemPrompt
                Map.of("role", "user", "content", userMessage)
        ));

        try {
            Map<?, ?> response = restTemplate.postForObject(ollamaApiUrl, requestBody, Map.class); // Sử dụng biến ollamaApiUrl

            if (response != null && response.containsKey("message")) {
                Map<?, ?> aiMessage = (Map<?, ?>) response.get("message");
                return (String) aiMessage.get("content");
            }

            return "Không có phản hồi từ AI.";

        } catch (Exception e) {
            System.err.println("Lỗi kết nối Chatbot AI: " + e.getMessage());
            return "Rất tiếc, trợ lý AI hiện đang không khả dụng. Vui lòng thử lại sau.";
        }
    }
}