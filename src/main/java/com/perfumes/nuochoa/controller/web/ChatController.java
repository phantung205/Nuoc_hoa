package com.perfumes.nuochoa.controller.web;

import com.perfumes.nuochoa.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatbotService chatbotService;

    @PostMapping
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> payload) {
        String userMsg = payload.get("message");
        String aiReply = chatbotService.askOllama(userMsg);
        return ResponseEntity.ok(Map.of("reply", aiReply));
    }
}