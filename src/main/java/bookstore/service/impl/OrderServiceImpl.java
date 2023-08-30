package bookstore.service.impl;

import bookstore.dto.order.CreateOrderResponseDto;
import bookstore.dto.order.OrderResponseDto;
import bookstore.dto.order.UpdateOrderDto;
import bookstore.dto.orderitem.OrderItemResponseDto;
import bookstore.exception.EntityNotFoundException;
import bookstore.mapper.OrderItemMapper;
import bookstore.mapper.OrderMapper;
import bookstore.model.CartItem;
import bookstore.model.Order;
import bookstore.model.OrderItem;
import bookstore.model.ShoppingCart;
import bookstore.model.User;
import bookstore.repository.OrderRepository;
import bookstore.service.OrderService;
import bookstore.service.ShoppingCartService;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {
    private final ShoppingCartService shoppingCartService;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Override
    @Transactional
    public CreateOrderResponseDto createOrder(User user) {
        Order newOrder = new Order();
        newOrder.setUser(user);
        newOrder.setOrderDate(LocalDateTime.now());
        newOrder.setStatus(Order.Status.CREATED);
        newOrder.setShippingAddress(user.getShippingAddress());

        ShoppingCart shoppingCart = shoppingCartService.getShoppingCartById(user.getId());
        Set<OrderItem> orderItems = shoppingCart.getCartItems().stream()
                .map(c -> convertToOrderItem(c, newOrder)).collect(Collectors.toSet());
        newOrder.setOrderItems(orderItems);

        BigDecimal total = orderItems.stream()
                .map(o -> o.getPrice().multiply(BigDecimal.valueOf(o.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        newOrder.setTotal(total);

        orderRepository.save(newOrder);
        shoppingCartService.clearShoppingCart(shoppingCart);

        return orderMapper.toCreateDto(newOrder);
    }

    @Override
    public UpdateOrderDto updateOrderStatus(Long id, UpdateOrderDto orderDto) {
        Order orderFromDb = orderRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find order by id " + id));
        Order model = orderMapper.toModel(orderDto);
        orderFromDb.setStatus(model.getStatus());
        orderRepository.save(orderFromDb);
        return orderMapper.toUpdateDto(orderFromDb);
    }

    @Override
    public OrderItemResponseDto getOrderItemByOrderIdAndItemId(
            Long userId,
            Long orderId,
            Long itemId
    ) {
        try {
            Order order = orderRepository.findByIdAndUser_IdAAndOrderItem_Id(orderId,
                    userId, itemId).get();
            return order.getOrderItems().stream()
                    .map(o -> orderItemMapper.toDto(o))
                    .findFirst().get();
        } catch (NoSuchElementException e) {
            throw new EntityNotFoundException("Can't find order by orderId " + orderId
                    + " and itemId " + itemId);
        }
    }

    @Override
    public List<OrderResponseDto> getOrders(Long userId) {
        List<Order> ordersByUserId = orderRepository.findAllByUser_Id(userId);
        return ordersByUserId.stream()
                .map(o -> orderMapper.toDto(o))
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderItemResponseDto> getOrderItemsByOrderId(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUser_Id(orderId, userId).orElseThrow(
                () -> new EntityNotFoundException("Can't find order by id " + orderId));
        return order.getOrderItems().stream()
                .map(o -> orderItemMapper.toDto(o))
                .collect(Collectors.toList());
    }

    private OrderItem convertToOrderItem(CartItem cartItem, Order order) {
        OrderItem orderItem = new OrderItem();
        orderItem.setBook(cartItem.getBook());
        orderItem.setPrice(cartItem.getBook().getPrice());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setOrder(order);
        return orderItem;
    }
}
