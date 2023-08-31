package bookstore.mapper;

import bookstore.config.MapperConfig;
import bookstore.dto.order.CreateOrderResponseDto;
import bookstore.dto.order.OrderResponseDto;
import bookstore.dto.order.UpdateOrderDto;
import bookstore.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class, uses = OrderItemMapper.class)
public interface OrderMapper {
    CreateOrderResponseDto toCreateDto(Order order);

    UpdateOrderDto toUpdateDto(Order order);

    Order toModel(UpdateOrderDto orderDto);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "orderItems", target = "orderItems")
    OrderResponseDto toDto(Order order);
}

