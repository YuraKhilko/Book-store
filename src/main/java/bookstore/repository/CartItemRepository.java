package bookstore.repository;

import bookstore.model.CartItem;
import org.hibernate.annotations.SQLDelete;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    @SQLDelete(sql = "DELETE FROM cart_items WHERE shopping_cart_id = :shoppingCartId")
    void deleteByShoppingCart_Id(Long shoppingCartId);
}
