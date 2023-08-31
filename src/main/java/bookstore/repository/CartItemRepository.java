package bookstore.repository;

import bookstore.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.shoppingCart.id = :shoppingCartId")
    void deleteByShoppingCart_Id(Long shoppingCartId);
}
