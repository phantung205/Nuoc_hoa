package com.perfumes.nuochoa.controller.web;

import com.perfumes.nuochoa.service.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller xử lý tin nhắn chatbot từ giao diện người dùng.
 *
 * Endpoint: POST /api/chat
 * Request body: { "message": "Tôi muốn tìm nước hoa nam" }
 * Response body: { "reply": "Câu trả lời từ AI..." }
 *
 * Không yêu cầu đăng nhập – ai cũng có thể chat (cấu hình trong SecurityConfig).
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatbotService chatbotService;

    // Constructor injection – nhất quán với toàn bộ project (không dùng @Autowired)
    public ChatController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    /**
     * Nhận tin nhắn từ người dùng, gửi tới AI Ollama và trả về câu trả lời.
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> payload) {
        String userMessage = payload.get("message");
        String aiReply = chatbotService.askOllama(userMessage);
        return ResponseEntity.ok(Map.of("reply", aiReply));
    }
}