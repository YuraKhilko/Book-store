package bookstore.service;

import bookstore.dto.cartitem.CreateCartItemRequestDto;
import bookstore.dto.cartitem.UpdateCartItemRequestDto;
import bookstore.dto.shoppingcart.ShoppingCartResponseDto;

public interface ShoppingCartService {
    ShoppingCartResponseDto getById(Long userId);

    ShoppingCartResponseDto addCartItem(Long userId, CreateCartItemRequestDto requestDto);

    ShoppingCartResponseDto updateCartItem(Long userId, Long cartItemId,
                                           UpdateCartItemRequestDto requestDto);

    ShoppingCartResponseDto deleteCartItem(Long userId, Long cartItemId);
}
