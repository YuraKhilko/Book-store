package bookstore.repository;

import bookstore.model.Category;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @EntityGraph(attributePaths = "books")
    Optional<Category> findById(Long id);
}
