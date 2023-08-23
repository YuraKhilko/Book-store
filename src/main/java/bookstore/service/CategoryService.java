package bookstore.service;

import bookstore.dto.category.CategoryDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    CategoryDto save(CategoryDto requestDto);

    CategoryDto findById(Long id);

    List<CategoryDto> findAll(Pageable pageable);

    CategoryDto update(Long id, CategoryDto requestDto);

    void deleteById(Long id);
}
