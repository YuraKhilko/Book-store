package bookstore.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bookstore.dto.cartitem.CartItemResponseDto;
import bookstore.dto.cartitem.CreateCartItemRequestDto;
import bookstore.dto.cartitem.UpdateCartItemRequestDto;
import bookstore.dto.shoppingcart.ShoppingCartResponseDto;
import bookstore.model.Role;
import bookstore.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShoppingCartControllerTest {
    protected static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(
            @Autowired DataSource dataSource,
            @Autowired WebApplicationContext applicationContext
    ) throws SQLException {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource(
                            "database/users/insert-test-users-with-shopping-carts.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/books/insert-test-books.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/cartitems/insert-1-test-cart-item.sql")
            );

        }
    }

    @AfterAll
    static void afterAll(
            @Autowired DataSource dataSource
    ) {
        teardown(dataSource);
    }

    @SneakyThrows
    private static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource(
                            "database/cartitems/delete-all-test-cart-items.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource(
                            "database/users/delete-all-test-users-with-shopping-carts.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource(
                            "database/books/delete-all-test-books.sql")
            );
        }
    }

    @Test
    @DisplayName("""
            Get user's shopping cart
            """)
    void getShoppingCart_GivenShoppingCartsCatalog_ReturnsUsersShoppingCart() throws Exception {
        // Given
        User mockUser = getMockUser();
        ShoppingCartResponseDto expected = getShoppingCartResponseDtoWithOneCartItem();

        // When
        MvcResult result = mockMvc.perform(get("/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(mockUser)))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        ShoppingCartResponseDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), ShoppingCartResponseDto.class);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            Add a new book to user's shopping cart
            """)
    @Sql(
            scripts = "classpath:database/cartitems/delete-1-test-cart-item.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    void addBookToShoppingCart_GivenShoppingCartAndNewBook_ReturnsUpdatedShoppingCart()
            throws Exception {
        // Given
        User mockUser = getMockUser();

        CreateCartItemRequestDto expected = new CreateCartItemRequestDto();
        expected.setBookId(3L);
        expected.setQuantity(3);

        String jsonRequest = objectMapper.writeValueAsString(expected);

        // When
        MvcResult result = mockMvc.perform(post("/cart")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(mockUser)))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        ShoppingCartResponseDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), ShoppingCartResponseDto.class);
        assertNotNull(actual);
        assertThat(actual.getCartItems()).hasSize(2);
        assertTrue(actual.getCartItems().stream()
                .anyMatch(c -> expected.getBookId().equals((c.getBookId()))
                        && expected.getQuantity() == c.getQuantity()));
    }

    @Test
    @DisplayName("""
            Update books quantity in user's shopping cart
            """)
    @Sql(
            scripts = "classpath:database/cartitems/update-1-test-cart-item.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    void updateCartItem_GivenShoppingCartWithItem_ReturnsShoppingCartWithUpdatedBookQuantity()
            throws Exception {
        // Given
        User mockUser = getMockUser();

        Long cartItemId = 1L;
        UpdateCartItemRequestDto expected = new UpdateCartItemRequestDto();
        expected.setQuantity(21);

        String jsonRequest = objectMapper.writeValueAsString(expected);

        // When
        MvcResult result = mockMvc.perform(put("/cart/cart-items/" + cartItemId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(mockUser)))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        ShoppingCartResponseDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), ShoppingCartResponseDto.class);
        assertNotNull(actual);
        assertThat(actual.getCartItems()).hasSize(1);
        assertTrue(actual.getCartItems().stream()
                .anyMatch(c -> cartItemId.equals((c.getBookId()))
                        && expected.getQuantity() == c.getQuantity()));
    }

    @Test
    @DisplayName("""
            Delete book from user's shopping cart
            """)
    @Sql(
            scripts = "classpath:database/cartitems/insert-1-test-cart-item.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    void deleteCartItem_GivenShoppingCartWithOneItem_ReturnsEmptyShoppingCart() throws Exception {
        // Given
        User mockUser = getMockUser();

        long cartItemId = 1L;

        // When
        MvcResult result = mockMvc.perform(delete("/cart/cart-items/" + cartItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(mockUser)))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        ShoppingCartResponseDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), ShoppingCartResponseDto.class);
        assertNotNull(actual);
        assertThat(actual.getCartItems()).hasSize(0);
    }

    private ShoppingCartResponseDto getShoppingCartResponseDtoWithOneCartItem() {
        ShoppingCartResponseDto shoppingCartResponseDto = new ShoppingCartResponseDto();
        shoppingCartResponseDto.setId(1L);
        shoppingCartResponseDto.setUserId(1L);

        CartItemResponseDto cartItemResponseDto = new CartItemResponseDto();
        cartItemResponseDto.setId(1L);
        cartItemResponseDto.setBookId(1L);
        cartItemResponseDto.setBookTitle("Title1");
        cartItemResponseDto.setQuantity(1);

        Set<CartItemResponseDto> cartItems = new HashSet<>();
        cartItems.add(cartItemResponseDto);
        shoppingCartResponseDto.setCartItems(cartItems);
        return shoppingCartResponseDto;
    }

    private User getMockUser() {
        User user = new User();
        user.setId(1L);
        Set<Role> roles = new HashSet<>();
        Role role = new Role();
        role.setName(Role.RoleName.USER);
        roles.add(role);
        user.setRoles(roles);
        return user;
    }
}
