package bookstore.repository.book.spec;

import bookstore.model.Book;
import bookstore.repository.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class PriceSpecificationProvider implements SpecificationProvider<Book> {
    public Specification<Book> getSpecification(String[] params) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.between(root.get("price"), params[0], params[1]);
        };
    }

    @Override
    public String getKey() {
        return "price";
    }
}
