package bookstore.mapper;

import bookstore.config.MapperConfig;
import bookstore.dto.orderitem.OrderItemResponseDto;
import bookstore.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface OrderItemMapper {
    @Mapping(target = "bookId", source = "book.id")
    OrderItemResponseDto toDto(OrderItem orderItem);
}
