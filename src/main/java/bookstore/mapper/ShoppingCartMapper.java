package bookstore.mapper;

import bookstore.config.MapperConfig;
import bookstore.dto.cartitem.CartItemResponseDtoForShoppingCart;
import bookstore.dto.shoppingcart.ShoppingCartResponseDto;
import bookstore.model.CartItem;
import bookstore.model.ShoppingCart;
import java.util.HashSet;
import java.util.Set;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface ShoppingCartMapper {
    ShoppingCartResponseDto toDto(ShoppingCart shoppingCart);

    @AfterMapping
    default void setShoppingCartResponseDtoParams(
            @MappingTarget ShoppingCartResponseDto shoppingCartResponseDto,
                           ShoppingCart shoppingCart
    ) {
        shoppingCartResponseDto.setUserId(shoppingCart.getUser().getId());
        Set<CartItemResponseDtoForShoppingCart> cartItemSet = new HashSet<>();
        for (CartItem item: shoppingCart.getCartItems()) {
            CartItemResponseDtoForShoppingCart cartItem = new CartItemResponseDtoForShoppingCart();
            cartItem.setId(item.getId());
            cartItem.setBookId(item.getBook().getId());
            cartItem.setBookTitle(item.getBook().getTitle());
            cartItem.setQuantity(item.getQuantity());
            cartItemSet.add(cartItem);
        }
        shoppingCartResponseDto.setCartItems(cartItemSet);
    }
}
