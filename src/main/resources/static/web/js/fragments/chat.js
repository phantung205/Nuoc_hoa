let hasGreeted = false;
let messageCounter = 0;

document.addEventListener('DOMContentLoaded', function() {
    loadChatHistory();
});

function loadChatHistory() {
    const savedLogs = localStorage.getItem('chat_logs');
    const savedGreeted = localStorage.getItem('chat_greeted');
    
    if (savedLogs) {
        const logs = document.getElementById("chat-logs");
        logs.innerHTML = savedLogs;
        
        // Remove any stuck loading messages if user refreshed while bot was thinking
        const stuckLoaders = logs.querySelectorAll('.chat-msg.loading');
        stuckLoaders.forEach(el => el.remove());
        
        logs.scrollTop = logs.scrollHeight;
    }
    
    if (savedGreeted === 'true') {
        hasGreeted = true;
    }
}

function saveChatHistory() {
    const logs = document.getElementById("chat-logs");
    localStorage.setItem('chat_logs', logs.innerHTML);
    localStorage.setItem('chat_greeted', hasGreeted);
}

function toggleChat() {
    const widget = document.getElementById("chat-popup");
    widget.classList.toggle("active");

    if (!hasGreeted && widget.classList.contains("active")) {
        appendMessage("Chào bạn! 🤖, Tôi là trợ lý AI của Cửa Hàng Nước Hoa. Bạn cần tư vấn thông tin gì hôm nay?", "bot");
        hasGreeted = true;
        saveChatHistory();
    }
}

function clearChat() {
    if (confirm("Bạn có chắc chắn muốn xóa toàn bộ lịch sử trò chuyện không?")) {
        localStorage.removeItem('chat_logs');
        localStorage.removeItem('chat_greeted');
        hasGreeted = false;
        
        const logs = document.getElementById("chat-logs");
        logs.innerHTML = '';
        
        // Nếu cửa sổ đang mở thì gửi lại lời chào
        const widget = document.getElementById("chat-popup");
        if (widget.classList.contains("active")) {
            appendMessage("Chào bạn! 🤖, Tôi là trợ lý AI của Cửa Hàng Nước Hoa. Bạn cần tư vấn thông tin gì hôm nay?", "bot");
            hasGreeted = true;
            saveChatHistory();
        }
    }
}

async function sendMessage() {
    const input = document.getElementById("user-input");
    const message = input.value.trim();
    if (!message) return;

    appendMessage(message, "user");
    input.value = "";
    saveChatHistory();

    const loadingId = appendMessage("Đang suy nghĩ...", "bot loading");
    saveChatHistory();

    try {
        const response = await fetch("/api/chat", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ message: message })
        });

        const data = await response.json();

        const loadingElement = document.getElementById(loadingId);
        if (loadingElement) {
            loadingElement.classList.remove("loading");
            // If the bot returns HTML formatting, we can use innerHTML, otherwise textContent.
            // Using textContent to prevent XSS is safer, but if it has markdown we might need to parse it.
            loadingElement.textContent = data.reply;
            saveChatHistory();
        }
    } catch (error) {
        const loadingElement = document.getElementById(loadingId);
        if (loadingElement) {
            loadingElement.classList.remove("loading");
            loadingElement.textContent = "Lỗi kết nối đến máy chủ AI!";
            saveChatHistory();
        }
    }
}

function appendMessage(text, type) {
    const logs = document.getElementById("chat-logs");
    const msgId = "msg-" + Date.now() + "-" + (++messageCounter);

    const msgDiv = document.createElement("div");
    msgDiv.id = msgId;
    // Tách class "bot" và "loading" ra đàng hoàng
    if (type.includes("loading")) {
        msgDiv.className = "chat-msg bot loading";
    } else {
        msgDiv.className = `chat-msg ${type}`;
    }
    msgDiv.textContent = text;

    logs.appendChild(msgDiv);
    logs.scrollTop = logs.scrollHeight;
    return msgId;
}