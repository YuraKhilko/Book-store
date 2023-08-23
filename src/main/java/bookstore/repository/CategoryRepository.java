package bookstore.repository;

import bookstore.model.Category;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("FROM Category c LEFT JOIN FETCH c.books WHERE c.id = :id")
    Optional<Category> findById(Long id);
}
