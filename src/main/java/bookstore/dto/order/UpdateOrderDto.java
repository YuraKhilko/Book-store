package bookstore.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateOrderDto {
    @NotBlank
    private String status;
}
