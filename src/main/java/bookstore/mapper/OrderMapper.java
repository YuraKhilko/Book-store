package bookstore.mapper;

import bookstore.config.MapperConfig;
import bookstore.dto.order.CreateOrderResponseDto;
import bookstore.dto.order.OrderResponseDto;
import bookstore.dto.order.UpdateOrderDto;
import bookstore.model.Order;
import org.apache.commons.lang3.EnumUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class, uses = OrderItemMapper.class)
public interface OrderMapper {
    CreateOrderResponseDto toCreateDto(Order order);

    @Mapping(source = "status", target = "status")
    UpdateOrderDto toUpdateDto(Order order);

    Order toModel(UpdateOrderDto orderDto);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "orderItems", target = "orderItems")
    OrderResponseDto toDto(Order order);

    @AfterMapping
    default void setStatus(@MappingTarget Order order, UpdateOrderDto orderDto) {
        Order.Status status =
                EnumUtils.getEnum(Order.Status.class, orderDto.getStatus().toUpperCase());
        order.setStatus(status);
    }
}

