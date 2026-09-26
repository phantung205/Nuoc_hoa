package com.perfumes.nuochoa.service;

import com.perfumes.nuochoa.entity.Order;
import com.perfumes.nuochoa.entity.UserAddress;

import java.util.List;

public interface OrderService {
    Order createOrder(String username, UserAddress shippingAddress, String paymentMethod, String note, Integer pointsToUse);
    List<Order> getOrdersByUsername(String username);
    Order getOrderById(Long orderId);
    void updateOrderStatus(Long orderId, String status);
    List<Order> getAllOrders();
    void deleteOrder(Long orderId);
    void cancelOrder(Long orderId, String username);
}
