let hasGreeted = false;
let messageCounter = 0; // Biến đếm chống trùng ID

function toggleChat() {
    const widget = document.getElementById("chat-popup");
    widget.classList.toggle("active");

    if (!hasGreeted && widget.classList.contains("active")) {
        appendMessage("Chào bạn! 🌸 Tôi là trợ lý AI của Cửa Hàng Nước Hoa. Bạn cần tư vấn thông tin gì hôm nay?", "bot");
        hasGreeted = true;
    }
}

async function sendMessage() {
    const input = document.getElementById("user-input");
    const message = input.value.trim();
    if (!message) return;

    // 1. Hiển thị câu hỏi của người dùng (Bên phải - Màu xanh)
    appendMessage(message, "user");
    input.value = "";

    // 2. Tạo ô "Đang suy nghĩ..." cho Bot (Bên trái - Màu trắng)
    const loadingId = appendMessage("Đang suy nghĩ...", "bot loading");

    try {
        const response = await fetch("/api/chat", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ message: message })
        });

        const data = await response.json();

        // 3. Cập nhật câu trả lời từ AI vào ĐÚNG ô của Bot
        const loadingElement = document.getElementById(loadingId);
        if (loadingElement) {
            loadingElement.classList.remove("loading");
            loadingElement.textContent = data.reply;
        }
    } catch (error) {
        const loadingElement = document.getElementById(loadingId);
        if (loadingElement) {
            loadingElement.classList.remove("loading");
            loadingElement.textContent = "Lỗi kết nối đến máy chủ AI!";
        }
    }
}

function appendMessage(text, type) {
    const logs = document.getElementById("chat-logs");
    // Kết hợp timestamp + counter để ID luôn là duy nhất
    const msgId = "msg-" + Date.now() + "-" + (++messageCounter);

    const msgDiv = document.createElement("div");
    msgDiv.id = msgId;
    msgDiv.className = `chat-msg ${type}`;
    msgDiv.textContent = text;

    logs.appendChild(msgDiv);
    logs.scrollTop = logs.scrollHeight;
    return msgId;
}