package com.perfumes.nuochoa.controller.web;

import com.perfumes.nuochoa.service.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatbotService chatbotService;

    // Constructor injection – nhất quán với toàn bộ project (không dùng @Autowired)
    public ChatController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }


    @PostMapping
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> payload) {
        String userMessage = payload.get("message");
        String aiReply = chatbotService.askOllama(userMessage);
        return ResponseEntity.ok(Map.of("reply", aiReply));
    }
}