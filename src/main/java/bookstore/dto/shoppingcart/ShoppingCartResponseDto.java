package bookstore.dto.shoppingcart;

import bookstore.dto.cartitem.CartItemResponseDtoForShoppingCart;
import java.util.Set;
import lombok.Data;

@Data
public class ShoppingCartResponseDto {
    private Long id;
    private Long userId;
    private Set<CartItemResponseDtoForShoppingCart> cartItems;
}
