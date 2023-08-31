package bookstore.service;

import bookstore.dto.order.CreateOrderResponseDto;
import bookstore.dto.order.OrderResponseDto;
import bookstore.dto.order.UpdateOrderDto;
import bookstore.dto.orderitem.OrderItemResponseDto;
import bookstore.model.User;
import java.util.List;

public interface OrderService {
    CreateOrderResponseDto createOrder(User user);

    UpdateOrderDto updateOrderStatus(Long id, UpdateOrderDto orderDto);

    OrderItemResponseDto getOrderItemByOrderIdAndItemId(Long userId, Long orderId, Long itemId);

    List<OrderResponseDto> getOrders(Long userId);

    List<OrderItemResponseDto> getOrderItemsByOrderId(Long userId, Long orderId);
}
