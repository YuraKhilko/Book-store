package bookstore.service;

import bookstore.dto.cartitem.CreateCartItemRequestDto;
import bookstore.dto.cartitem.UpdateCartItemRequestDto;
import bookstore.dto.shoppingcart.ShoppingCartResponseDto;

public interface ShoppingCartService {
    ShoppingCartResponseDto getShoppingCart(String email);

    ShoppingCartResponseDto addCartItem(String email, CreateCartItemRequestDto requestDto);

    ShoppingCartResponseDto updateCartItem(String email, Long cartItemId,
                                           UpdateCartItemRequestDto requestDto);

    ShoppingCartResponseDto deleteCartItem(String email, Long cartItemId);
}
