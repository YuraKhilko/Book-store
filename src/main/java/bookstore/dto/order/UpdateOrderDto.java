package bookstore.dto.order;

import bookstore.model.Order;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderDto {
    @NotNull
    private Order.Status status;
}
