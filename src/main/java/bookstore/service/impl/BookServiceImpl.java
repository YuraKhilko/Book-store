package bookstore.service.impl;

import bookstore.model.Book;
import bookstore.repository.BookRepository;
import bookstore.service.BookService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;

    @Autowired
    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public Book save(Book product) {
        return bookRepository.save(product);
    }

    @Override
    public List findAll() {
        return bookRepository.findAll();
    }
}
