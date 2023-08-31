package bookstore.repository;

import bookstore.model.OrderItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("FROM OrderItem oi "
            + "INNER JOIN FETCH oi.order o "
            + "INNER JOIN FETCH o.user u "
            + "WHERE oi.id = :itemId "
            + "AND o.id = :orderId "
            + "AND u.id = :userId")
    Optional<OrderItem> findByIdAndUserIdAndOrderId(Long orderId, Long userId, Long itemId);
}
