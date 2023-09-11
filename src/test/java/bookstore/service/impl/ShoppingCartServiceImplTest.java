package bookstore.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import bookstore.dto.cartitem.CartItemResponseDto;
import bookstore.dto.cartitem.CreateCartItemRequestDto;
import bookstore.dto.cartitem.UpdateCartItemRequestDto;
import bookstore.dto.shoppingcart.ShoppingCartResponseDto;
import bookstore.exception.DuplicateEntityException;
import bookstore.exception.EntityNotFoundException;
import bookstore.mapper.CartItemMapper;
import bookstore.mapper.ShoppingCartMapper;
import bookstore.model.Book;
import bookstore.model.CartItem;
import bookstore.model.ShoppingCart;
import bookstore.model.User;
import bookstore.repository.CartItemRepository;
import bookstore.repository.ShoppingCartRepository;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceImplTest {
    @InjectMocks
    private ShoppingCartServiceImpl shoppingCartServiceImpl;
    @Mock
    private ShoppingCartRepository shoppingCartRepository;
    @Mock
    private ShoppingCartMapper shoppingCartMapper;
    @Mock
    private CartItemMapper cartItemMapper;
    @Mock
    private CartItemRepository cartItemRepository;

    @Test
    @DisplayName("""
            Verify getById() method works and returns ShoppingCartResponseDto by userId
            """)
    void getById_ValidUserId_ReturnsValidShoppingCartResponseDto() {
        // Given
        ShoppingCart shoppingCart = getShoppingCartWithOneCartItem();
        Long userId = shoppingCart.getUser().getId();

        ShoppingCartResponseDto expected = getShoppingCartResponseDtoByShoppingCart(shoppingCart);

        Mockito.when(shoppingCartRepository.findById(userId)).thenReturn(Optional.of(shoppingCart));
        Mockito.when(shoppingCartMapper.toDto(shoppingCart)).thenReturn(expected);

        // When
        ShoppingCartResponseDto actual =
                shoppingCartServiceImpl.getById(userId);

        // Then
        assertEquals(expected, actual);
        verify(shoppingCartRepository, times(1)).findById(userId);
        verify(shoppingCartMapper, times(1)).toDto(shoppingCart);
        verifyNoMoreInteractions(shoppingCartRepository, shoppingCartMapper);
    }

    @Test
    @DisplayName("""
            Verify addCartItem() method works and returns ShoppingCartResponseDto 
            with added cart item
            """)
    void addCartItem_ValidCartItem_ReturnsShoppingCartResponseDtoWithAddedCartItem() {
        // Given
        ShoppingCart shoppingCart = getShoppingCartWithOneCartItem();
        final Long userId = shoppingCart.getUser().getId();

        ShoppingCart shoppingCartWithNewItem = new ShoppingCart();
        shoppingCartWithNewItem.setId(shoppingCart.getId());
        shoppingCartWithNewItem.setUser(shoppingCart.getUser());
        Set<CartItem> updatedCartItemsSet = new HashSet<>(shoppingCart.getCartItems());
        shoppingCartWithNewItem.setCartItems(updatedCartItemsSet);

        CartItem cartItem = getNewCartItem(shoppingCart);

        shoppingCartWithNewItem.getCartItems().add(cartItem);

        ShoppingCartResponseDto expected = getShoppingCartResponseDtoByShoppingCart(shoppingCart);

        CreateCartItemRequestDto createCartItemRequestDto =
                getCreateCartItemRequestDtoByCartItem(cartItem);

        Mockito.when(cartItemMapper.toEntity(createCartItemRequestDto)).thenReturn(cartItem);
        Mockito.when(shoppingCartRepository.findById(userId)).thenReturn(
                Optional.of(shoppingCart), Optional.of(shoppingCartWithNewItem));
        Mockito.when(shoppingCartMapper.toDto(shoppingCartWithNewItem)).thenReturn(expected);

        // When
        ShoppingCartResponseDto actual =
                shoppingCartServiceImpl.addCartItem(userId, createCartItemRequestDto);

        // Then
        assertEquals(expected, actual);
        verify(shoppingCartRepository, times(2)).findById(userId);
        verify(shoppingCartMapper, times(1)).toDto(shoppingCartWithNewItem);
        verify(cartItemMapper, times(1)).toEntity(createCartItemRequestDto);
        verify(cartItemRepository, times(1)).save(cartItem);
        verifyNoMoreInteractions(shoppingCartRepository, shoppingCartMapper, cartItemMapper,
                cartItemRepository);
    }

    @Test
    @DisplayName("""
            Verify addCartItem() throws DuplicateEntityException while trying to add a new book, 
            which jas already been added
            """)
    void addCartItem_DuplicateCartItem_ThrowsDuplicateEntityException() {
        // Given
        ShoppingCart shoppingCart = getShoppingCartWithOneCartItem();
        Long userId = shoppingCart.getUser().getId();

        CartItem duplicateCartItem = getDuplicateCartItem(shoppingCart);

        CreateCartItemRequestDto duplicateCreateCartItemRequestDto =
                getCreateCartItemRequestDtoByCartItem(duplicateCartItem);

        Mockito.when(cartItemMapper.toEntity(duplicateCreateCartItemRequestDto)).thenReturn(
                duplicateCartItem);
        Mockito.when(shoppingCartRepository.findById(userId)).thenReturn(
                Optional.of(shoppingCart));

        // When
        DuplicateEntityException exception =
                assertThrows(DuplicateEntityException.class,
                        () -> shoppingCartServiceImpl.addCartItem(
                                userId, duplicateCreateCartItemRequestDto));

        // Then
        String expected = "Book has been already added";
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verify(shoppingCartRepository, times(1)).findById(userId);
        verify(cartItemMapper, times(1)).toEntity(duplicateCreateCartItemRequestDto);
        verifyNoMoreInteractions(shoppingCartRepository, cartItemMapper);
    }

    @Test
    @DisplayName("""
            Verify updateCartItem() method works and returns ShoppingCartResponseDto 
            with updated cart item
            """)
    void updateCartItem_ValidUpdateCartItemRequestDto_ReturnsShoppingCartWithUpdatedCartItem() {
        // Given
        ShoppingCart shoppingCart = getShoppingCartWithOneCartItem();
        final Long userId = shoppingCart.getUser().getId();

        final Long cartItemId = 1L;

        UpdateCartItemRequestDto updateCartItemRequestDto = new UpdateCartItemRequestDto();
        updateCartItemRequestDto.setQuantity(11);

        CartItem updateCartItem = new CartItem();
        updateCartItem.setQuantity(updateCartItemRequestDto.getQuantity());

        shoppingCart.getCartItems().forEach(
                s -> s.setQuantity(updateCartItemRequestDto.getQuantity()));

        ShoppingCartResponseDto expected = getShoppingCartResponseDtoByShoppingCart(shoppingCart);

        CartItem cartItemFromDb = shoppingCart.getCartItems().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Can't get test CartItem"));

        Mockito.when(cartItemRepository.findById(cartItemId))
                        .thenReturn(Optional.of(cartItemFromDb));
        Mockito.when(cartItemMapper.toEntity(updateCartItemRequestDto)).thenReturn(updateCartItem);
        Mockito.when(shoppingCartRepository.findById(userId)).thenReturn(Optional.of(shoppingCart));
        Mockito.when(shoppingCartMapper.toDto(shoppingCart)).thenReturn(expected);

        // When
        ShoppingCartResponseDto actual =
                shoppingCartServiceImpl.updateCartItem(userId, cartItemId,
                        updateCartItemRequestDto);
        // Then
        assertEquals(expected, actual);
        verify(cartItemRepository, times(1)).save(any());
        verify(cartItemMapper, times(1)).toEntity(updateCartItemRequestDto);
        verify(shoppingCartRepository, times(1)).findById(userId);
        verify(shoppingCartMapper, times(1)).toDto(shoppingCart);
        verifyNoMoreInteractions(shoppingCartRepository, shoppingCartMapper, cartItemMapper,
                cartItemRepository);
    }

    @Test
    @DisplayName("""
            Verify updateCartItem() throws EntityNotFoundException while 
            trying to update not existing book cart item
            """)
    void updateCartItem_NotExistedCartItemId_ThrowsEntityNotFoundException() {
        // Given
        ShoppingCart shoppingCart = getShoppingCartWithOneCartItem();
        Long userId = shoppingCart.getUser().getId();

        Long cartItemId = 1L;

        Mockito.when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.empty());

        // When
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class,
                        () -> shoppingCartServiceImpl.updateCartItem(userId, cartItemId,
                                any()));
        // Then
        String expected = "Can't find cart item by id " + cartItemId;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verify(cartItemRepository, times(1)).findById(cartItemId);
        verifyNoMoreInteractions(cartItemRepository);
    }

    @Test
    @DisplayName("""
            Verify deleteCartItem() method works and returns ShoppingCartResponseDto 
            without excluded cart item
            """)
    void deleteCartItem_ValidCartItemId_ReturnsShoppingCartResponseDtoWithoutExcludedCartItem() {
        // Given
        Long cartItemId = 1L;
        ShoppingCart shoppingCart = getShoppingCartWithOneCartItem();
        shoppingCart.getCartItems().clear();
        Long userId = shoppingCart.getUser().getId();

        ShoppingCartResponseDto expected = getShoppingCartResponseDtoByShoppingCart(shoppingCart);

        Mockito.when(shoppingCartRepository.findById(userId)).thenReturn(Optional.of(shoppingCart));
        Mockito.when(shoppingCartMapper.toDto(shoppingCart)).thenReturn(expected);

        // When
        ShoppingCartResponseDto actual =
                shoppingCartServiceImpl.deleteCartItem(userId, cartItemId);

        // Then
        assertEquals(expected, actual);
        verify(cartItemRepository, times(1)).deleteById(cartItemId);
        verify(shoppingCartRepository, times(1)).findById(userId);
        verify(shoppingCartMapper, times(1)).toDto(shoppingCart);
        verifyNoMoreInteractions(cartItemRepository, shoppingCartRepository, shoppingCartMapper);
    }

    @Test
    @DisplayName("""
            Verify clearShoppingCart() method works
            """)
    void clearShoppingCart_ValidShoppingCart_DoesNotThrowException() {
        //Given
        ShoppingCart shoppingCart = getShoppingCartWithOneCartItem();

        // Then
        assertDoesNotThrow(() -> shoppingCartServiceImpl.clearShoppingCart(shoppingCart));
    }

    @Test
    @DisplayName("""
            Verify findById() method works and returns shopping cart by userId
            """)
    void getShoppingCartById_ValidUserId_ReturnsValidShoppingCart() {
        // Given
        ShoppingCart expected = getShoppingCartWithOneCartItem();
        Long shoppingCartId = expected.getId();

        Mockito.when(shoppingCartRepository.findById(shoppingCartId))
                        .thenReturn(Optional.of(expected));

        // When
        ShoppingCart actual =
                shoppingCartServiceImpl.getShoppingCartById(shoppingCartId);

        // Then
        assertEquals(expected, actual);
        verify(shoppingCartRepository, times(1)).findById(shoppingCartId);
        verifyNoMoreInteractions(shoppingCartRepository);
    }

    @Test
    @DisplayName("""
            Verify the EntityNotFoundException was thrown for invalid userId
            """)
    void getShoppingCartById_InvalidUserId_ThrowsEntityNotFoundException() {
        // Given
        Long userId = -1L;
        Mockito.when(shoppingCartRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class,
                        () -> shoppingCartServiceImpl.getShoppingCartById(userId));

        // Then
        String expected = "Can't find shopping cart by userId " + userId;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verify(shoppingCartRepository, times(1)).findById(userId);
        verifyNoMoreInteractions(shoppingCartRepository);
    }

    private ShoppingCartResponseDto getShoppingCartResponseDtoByShoppingCart(
            ShoppingCart shoppingCart
    ) {
        Set<CartItemResponseDto> cartItemResponseDtoSet = shoppingCart.getCartItems().stream()
                .map(c -> new CartItemResponseDto()
                        .setId(c.getId())
                        .setBookId(c.getBook().getId())
                        .setBookTitle(c.getBook().getTitle())
                        .setQuantity(c.getQuantity()))
                .collect(Collectors.toSet());

        ShoppingCartResponseDto shoppingCartResponseDto = new ShoppingCartResponseDto();
        shoppingCartResponseDto.setUserId(shoppingCart.getUser().getId());
        shoppingCartResponseDto.setId(shoppingCart.getId());
        shoppingCartResponseDto.setCartItems(cartItemResponseDtoSet);
        return shoppingCartResponseDto;
    }

    private ShoppingCart getShoppingCartWithOneCartItem() {
        User user = new User();
        user.setId(1L);
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Title1");

        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setQuantity(1);
        cartItem.setBook(book);

        Set<CartItem> cartItems = new HashSet<>();
        cartItems.add(cartItem);

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);
        shoppingCart.setId(user.getId());
        shoppingCart.setCartItems(cartItems);
        return shoppingCart;
    }

    private CreateCartItemRequestDto getCreateCartItemRequestDtoByCartItem(CartItem cartItem) {
        return new CreateCartItemRequestDto()
                .setBookId(cartItem.getBook().getId())
                .setQuantity(cartItem.getQuantity());
    }

    private CartItem getNewCartItem(ShoppingCart shoppingCart) {
        Book book = new Book();
        book.setId(3L);
        book.setTitle("Title3");

        CartItem cartItem = new CartItem();
        cartItem.setShoppingCart(shoppingCart);
        cartItem.setQuantity(4);
        cartItem.setBook(book);
        return cartItem;
    }

    private CartItem getDuplicateCartItem(ShoppingCart shoppingCart) {
        CartItem cartItem = getNewCartItem(shoppingCart);
        cartItem.getBook().setId(1L);
        return cartItem;
    }
}
