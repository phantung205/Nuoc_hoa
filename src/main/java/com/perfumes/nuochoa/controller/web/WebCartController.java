package com.perfumes.nuochoa.controller.web;

import com.perfumes.nuochoa.dto.CartItemDto;
import com.perfumes.nuochoa.entity.CartItem;
import com.perfumes.nuochoa.entity.ProductImage;
import com.perfumes.nuochoa.repository.ProductImageRepository;
import com.perfumes.nuochoa.service.CartService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class WebCartController {

    private final CartService cartService;
    private final ProductImageRepository productImageRepository;

    public WebCartController(CartService cartService, ProductImageRepository productImageRepository) {
        this.cartService = cartService;
        this.productImageRepository = productImageRepository;
    }

    @GetMapping
    public String viewCart(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        List<CartItem> cartItems = cartService.getCartItemsByUsername(username);
        List<CartItemDto> itemDtos = new ArrayList<>();
        Double totalAmount = 0.0;
        
        for (CartItem item : cartItems) {
            CartItemDto dto = new CartItemDto();
            dto.setItemId(item.getId());
            dto.setProductId(item.getProductVariant().getProduct().getId());
            dto.setProductName(item.getProductVariant().getProduct().getName());
            dto.setVolume(item.getProductVariant().getVolume());
            dto.setConcentration(item.getProductVariant().getConcentration());
            
            Double price = item.getProductVariant().getPrice();
            dto.setPrice(price);
            dto.setQuantity(item.getQuantity());
            
            Double itemTotal = price * item.getQuantity();
            dto.setTotalPrice(itemTotal);
            totalAmount = totalAmount + itemTotal;
            
            // Get primary image
            List<ProductImage> images = productImageRepository.findByProductId(dto.getProductId());
            Optional<ProductImage> primaryImg = images.stream().filter(ProductImage::getIsPrimary).findFirst();
            if (primaryImg.isPresent()) {
                dto.setImageUrl(primaryImg.get().getImageUrl());
            } else if (!images.isEmpty()) {
                dto.setImageUrl(images.get(0).getImageUrl());
            } else {
                dto.setImageUrl("/web/images/default-product.jpg");
            }
            
            itemDtos.add(dto);
        }
        
        model.addAttribute("cartItems", itemDtos);
        model.addAttribute("totalAmount", totalAmount);
        
        return "web/pages/cart/index";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam("variantId") Long variantId,
                            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                            RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        try {
            cartService.addToCart(username, variantId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm sản phẩm vào giỏ hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateCartItem(@RequestParam("itemId") Long itemId,
                                 @RequestParam("quantity") int quantity,
                                 RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        try {
            cartService.updateCartItem(username, itemId, quantity);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeCartItem(@RequestParam("itemId") Long itemId,
                                 RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        try {
            cartService.removeFromCart(username, itemId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm khỏi giỏ hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/cart";
    }
}
