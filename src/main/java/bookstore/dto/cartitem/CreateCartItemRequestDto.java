package bookstore.dto.cartitem;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateCartItemRequestDto {
    @Min(1)
    @NotNull
    private Long bookId;
    @Min(1)
    @NotNull
    private int quantity;
}
