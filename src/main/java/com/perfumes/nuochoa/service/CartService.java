package com.perfumes.nuochoa.service;

import com.perfumes.nuochoa.entity.Cart;
import com.perfumes.nuochoa.entity.CartItem;

import java.util.List;

public interface CartService {
    Cart getCartByUsername(String username);
    List<CartItem> getCartItemsByUsername(String username);
    void addToCart(String username, Long variantId, int quantity);
    void updateCartItem(String username, Long itemId, int quantity);
    void removeFromCart(String username, Long itemId);
    void clearCart(String username);
}
