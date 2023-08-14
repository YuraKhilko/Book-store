package bookstore.repository.book.spec;

import bookstore.model.Book;
import bookstore.repository.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class TitleSpecificationProvider implements SpecificationProvider<Book> {
    public Specification<Book> getSpecification(String[] params) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(root.get("title"), "%" + String.join(",", params) + "%");
        };
    }

    @Override
    public String getKey() {
        return "title";
    }
}
