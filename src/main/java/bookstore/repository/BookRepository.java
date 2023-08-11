package bookstore.repository;

import bookstore.model.Book;
import java.util.List;
import java.util.Optional;

public interface BookRepository {
    Book save(Book product);

    List<Book> findAll();

    Optional<Book> findById(Long id);
}

