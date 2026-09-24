package com.perfumes.nuochoa.controller.web;

import com.perfumes.nuochoa.dto.CartItemDto;
import com.perfumes.nuochoa.dto.UserAddressDTO;
import com.perfumes.nuochoa.entity.CartItem;
import com.perfumes.nuochoa.entity.ProductImage;
import com.perfumes.nuochoa.entity.UserAddress;
import com.perfumes.nuochoa.repository.ProductImageRepository;
import com.perfumes.nuochoa.service.CartService;
import com.perfumes.nuochoa.service.UserAddressService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/checkout")
public class WebCheckoutController {

    private final CartService cartService;
    private final UserAddressService userAddressService;
    private final ProductImageRepository productImageRepository;
    private final com.perfumes.nuochoa.service.OrderService orderService;
    private final com.perfumes.nuochoa.service.UserService userService;

    public WebCheckoutController(CartService cartService, 
                                 UserAddressService userAddressService, 
                                 ProductImageRepository productImageRepository,
                                 com.perfumes.nuochoa.service.OrderService orderService,
                                 com.perfumes.nuochoa.service.UserService userService) {
        this.cartService = cartService;
        this.userAddressService = userAddressService;
        this.productImageRepository = productImageRepository;
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping
    public String viewCheckout(Model model, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        List<CartItem> cartItems = cartService.getCartItemsByUsername(username);
        if (cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng của bạn đang trống, vui lòng chọn sản phẩm trước khi thanh toán.");
            return "redirect:/cart";
        }
        
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
            totalAmount += itemTotal;
            
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
        
        List<UserAddress> addresses = userAddressService.getUserAddresses(username);
        com.perfumes.nuochoa.entity.User user = userService.findByUsername(username);
        com.perfumes.nuochoa.entity.UserProfile profile = userService.getUserProfileByUserId(user.getId());
        model.addAttribute("userProfile", profile);
        
        model.addAttribute("cartItems", itemDtos);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("addresses", addresses);
        
        return "web/pages/checkout/index";
    }

    @PostMapping("/process")
    public String processCheckout(
            @RequestParam(required = false, name = "addressId") String addressIdStr,
            @RequestParam(required = false) String receiverName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String receiverAddress,
            @RequestParam(required = false) String note,
            @RequestParam(required = false, defaultValue = "false") boolean saveAddress,
            @RequestParam(required = false, defaultValue = "COD") String paymentMethod,
            @RequestParam(required = false, defaultValue = "0") Integer pointsToUse,
            RedirectAttributes redirectAttributes) {
            
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        Long addressId = null;
        if (addressIdStr != null && !addressIdStr.trim().isEmpty()) {
            try {
                addressId = Long.parseLong(addressIdStr);
            } catch (NumberFormatException e) {}
        }
        
        UserAddress finalAddress = null;
        if (addressId != null) {
            finalAddress = userAddressService.getAddressById(username, addressId);
        } else {
            if (receiverName == null || phoneNumber == null || receiverAddress == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập đầy đủ thông tin giao hàng!");
                return "redirect:/checkout";
            }
            
            UserAddressDTO newAddressDto = new UserAddressDTO();
            newAddressDto.setReceiverName(receiverName);
            newAddressDto.setPhoneNumber(phoneNumber);
            newAddressDto.setReceiverAddress(receiverAddress);
            newAddressDto.setNote(note);
            newAddressDto.setAddressType("Khác");
            
            if (saveAddress) {
                finalAddress = userAddressService.addAddress(username, newAddressDto);
            } else {
                finalAddress = new UserAddress();
                finalAddress.setReceiverName(receiverName);
                finalAddress.setPhoneNumber(phoneNumber);
                finalAddress.setReceiverAddress(receiverAddress);
                finalAddress.setNote(note);
                finalAddress.setAddressType("Khác");
            }
        }
        
        try {
            orderService.createOrder(username, finalAddress, paymentMethod, note, pointsToUse);
            redirectAttributes.addFlashAttribute("successMessage", "Đặt hàng thành công! Đơn hàng của bạn đang được xử lý.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/checkout";
        }
        
        return "redirect:/orders";
    }
}