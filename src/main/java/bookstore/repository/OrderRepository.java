package bookstore.repository;

import bookstore.model.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = "orderItems")
    List<Order> findAllByUser_Id(Long userId);

    @EntityGraph(attributePaths = "orderItems")
    Optional<Order> findByIdAndUser_Id(Long orderId, Long userId);

    @Query("FROM Order o "
            + "INNER JOIN FETCH o.orderItems oi "
            + "INNER JOIN FETCH o.user u "
            + "WHERE o.id = :orderId "
            + "AND u.id = :userId "
            + "AND oi.id = :itemId")
    Optional<Order> findByIdAndUser_IdAAndOrderItem_Id(Long orderId, Long userId, Long itemId);
}
