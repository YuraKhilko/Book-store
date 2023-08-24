package bookstore.dto.cartitem;

import lombok.Data;

@Data
public class CartItemResponseDtoForShoppingCart {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private Integer quantity;
}
