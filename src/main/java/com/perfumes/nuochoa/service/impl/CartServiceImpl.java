package com.perfumes.nuochoa.service.impl;

import com.perfumes.nuochoa.entity.Cart;
import com.perfumes.nuochoa.entity.CartItem;
import com.perfumes.nuochoa.entity.ProductVariant;
import com.perfumes.nuochoa.entity.User;
import com.perfumes.nuochoa.repository.CartItemRepository;
import com.perfumes.nuochoa.repository.CartRepository;
import com.perfumes.nuochoa.repository.ProductVariantRepository;
import com.perfumes.nuochoa.repository.UserRepository;
import com.perfumes.nuochoa.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;

    public CartServiceImpl(CartRepository cartRepository,
                           CartItemRepository cartItemRepository,
                           UserRepository userRepository,
                           ProductVariantRepository productVariantRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productVariantRepository = productVariantRepository;
    }

    @Override
    public Cart getCartByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    @Override
    public List<CartItem> getCartItemsByUsername(String username) {
        Cart cart = getCartByUsername(username);
        return cartItemRepository.findByCartId(cart.getId());
    }

    @Override
    @Transactional
    public void addToCart(String username, Long variantId, int quantity) {
        if (quantity <= 0) return;

        Cart cart = getCartByUsername(username);
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartIdAndProductVariantId(cart.getId(), variantId);

        if (existingItemOpt.isPresent()) {
            CartItem item = existingItemOpt.get();
            int newQuantity = item.getQuantity() + quantity;
            if (newQuantity > variant.getStock()) {
                throw new RuntimeException("Số lượng yêu cầu vượt quá số lượng tồn kho (Còn lại: " + variant.getStock() + ")");
            }
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            if (quantity > variant.getStock()) {
                throw new RuntimeException("Số lượng yêu cầu vượt quá số lượng tồn kho (Còn lại: " + variant.getStock() + ")");
            }
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProductVariant(variant);
            newItem.setQuantity(quantity);
            cartItemRepository.save(newItem);
        }
    }

    @Override
    @Transactional
    public void updateCartItem(String username, Long itemId, int quantity) {
        Cart cart = getCartByUsername(username);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Không có quyền cập nhật giỏ hàng này");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            ProductVariant variant = item.getProductVariant();
            if (quantity > variant.getStock()) {
                throw new RuntimeException("Số lượng yêu cầu vượt quá số lượng tồn kho (Còn lại: " + variant.getStock() + ")");
            }
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
    }

    @Override
    @Transactional
    public void removeFromCart(String username, Long itemId) {
        Cart cart = getCartByUsername(username);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Không có quyền xóa sản phẩm này");
        }

        cartItemRepository.delete(item);
    }

    @Override
    @Transactional
    public void clearCart(String username) {
        Cart cart = getCartByUsername(username);
        cartItemRepository.deleteByCartId(cart.getId());
    }
}
