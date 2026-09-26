package com.perfumes.nuochoa.service;

import com.perfumes.nuochoa.entity.ProductVariant;
import com.perfumes.nuochoa.repository.ProductVariantRepository;
import com.perfumes.nuochoa.entity.Brand;
import com.perfumes.nuochoa.entity.Category;
import com.perfumes.nuochoa.entity.Product;
import com.perfumes.nuochoa.repository.BrandRepository;
import com.perfumes.nuochoa.repository.CategoryRepository;
import com.perfumes.nuochoa.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.text.DecimalFormat;

@Service
public class ChatbotService {

    @Value("${chatbot.ollama.url}")
    private String ollamaApiUrl;

    @Value("${chatbot.ollama.model}")
    private String aiModel;

    @Value("${chatbot.ollama.system-prompt}")
    private String baseSystemPrompt;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public String askOllama(String userMessage) {
        
        // 1. Fetch dynamic data from database
        List<Brand> brands = brandRepository.findByIsActiveTrue();
        List<Category> categories = categoryRepository.findByIsActiveTrue();
        List<Product> products = productRepository.findByIsActiveTrue();
        List<ProductVariant> variants = productVariantRepository.findAll();

        // 2. Format into strings
        String brandList = brands.stream().map(Brand::getName).collect(Collectors.joining(", "));
        String categoryList = categories.stream().map(Category::getName).collect(Collectors.joining(", "));
        
        // Map variants by Product ID for quick lookup
        Map<Long, List<ProductVariant>> variantsByProduct = variants.stream()
                .filter(v -> v.getProduct() != null && v.getVolume() != null)
                .collect(Collectors.groupingBy(v -> v.getProduct().getId()));

        DecimalFormat currencyFormat = new DecimalFormat("#,###");

        // Limit to 100 products to prevent exceeding LLM context limits
        String productList = products.stream()
                .limit(100)
                .map(p -> {
                    String pName = p.getName();
                    String brandName = p.getBrand() != null ? p.getBrand().getName() : "Không rõ";
                    String catName = p.getCategory() != null ? p.getCategory().getName() : "Không rõ";
                    
                    // Lấy các biến thể của sản phẩm này (bao gồm dung tích, giá, và tồn kho)
                    List<ProductVariant> pVariants = variantsByProduct.getOrDefault(p.getId(), List.of());
                    String variantDetails = pVariants.isEmpty() ? "Đang cập nhật" : 
                            pVariants.stream().map(v -> {
                                String priceStr = v.getPrice() != null ? currencyFormat.format(v.getPrice()) + " VND" : "Liên hệ";
                                String stockStr = v.getStock() != null && v.getStock() > 0 ? "Còn hàng (" + v.getStock() + ")" : "Hết hàng";
                                return String.format("%sml (%s, %s)", v.getVolume(), priceStr, stockStr);
                            }).collect(Collectors.joining(" | "));
                    
                    return String.format("- %s (Thương hiệu: %s, Danh mục: %s) => CÁC PHIÊN BẢN: %s", pName, brandName, catName, variantDetails);
                })
                .collect(Collectors.joining("\n"));

        // 3. Inject into the system prompt
        String dynamicSystemPrompt = baseSystemPrompt + "\n\n" +
                "--- THÔNG TIN CỬA HÀNG 2T&C TỪ CƠ SỞ DỮ LIỆU ---\n" +
                "Danh mục: " + categoryList + "\n" +
                "Thương hiệu: " + brandList + "\n" +
                "Danh sách sản phẩm chi tiết (gồm dung tích ml, giá tiền và tình trạng kho):\n" + productList + "\n\n" +
                "QUY TẮC TƯ VẤN: Bạn HÃY sử dụng thông tin CƠ SỞ DỮ LIỆU ở trên để trả lời. Nếu khách hỏi giá, dung tích (ml) hoặc tình trạng kho, hãy báo chính xác theo danh sách. Nếu sản phẩm/phiên bản không có trong danh sách, hãy xin lỗi và báo là hiện cửa hàng chưa có hoặc đã hết hàng. Luôn gợi ý thêm các dung tích hoặc sản phẩm cùng thương hiệu để khách có nhiều sự lựa chọn.";

        // Xây dựng request body theo chuẩn Ollama Chat API
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiModel);
        requestBody.put("stream", false);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", dynamicSystemPrompt),
                Map.of("role", "user", "content", userMessage)
        ));

        try {
            Map<?, ?> response = restTemplate.postForObject(ollamaApiUrl, requestBody, Map.class);

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