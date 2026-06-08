package com.campus.exchange.service;

import com.campus.exchange.dto.CreateOrderDTO;
import com.campus.exchange.vo.OrderDetailVO;
import com.campus.exchange.vo.OrderVO;
import java.util.List;

public interface OrderService {
    OrderVO create(Long buyerId, CreateOrderDTO dto);
    OrderVO confirm(Long orderId, Long sellerId);
    OrderVO cancel(Long orderId, Long buyerId);
    OrderVO complete(Long orderId, Long buyerId);
    OrderVO requestCancel(Long orderId, Long userId, String reason);
    OrderVO approveCancel(Long orderId, Long userId);
    OrderVO rejectCancel(Long orderId, Long userId);
    List<OrderVO> getBuyerOrders(Long userId, String status, int page, int size);
    List<OrderVO> getSellerOrders(Long userId, String status, int page, int size);
    OrderDetailVO getDetail(Long orderId, Long userId);
}
