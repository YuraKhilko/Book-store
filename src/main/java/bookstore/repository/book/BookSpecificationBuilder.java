package bookstore.repository.book;

import bookstore.dto.BookSearchParametersDto;
import org.springframework.data.jpa.domain.Specification;

public interface BookSpecificationBuilder<T> {
    Specification<T> build(BookSearchParametersDto searchParameters);
}
