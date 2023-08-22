package bookstore.mapper;

import bookstore.config.MapperConfig;
import bookstore.dto.book.BookDto;
import bookstore.dto.book.BookDtoWithoutCategoryIds;
import bookstore.dto.book.CreateBookRequestDto;
import bookstore.model.Book;
import bookstore.service.impl.CategoryServiceImpl;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class, uses = CategoryServiceImpl.class)
public interface BookMapper {

    BookDto toDto(Book book);

    @Mapping(target = "categories",
            qualifiedByName = { "CategoryService", "getSetCategoriesByIds" })
    Book toEntity(CreateBookRequestDto requestDto);

    BookDtoWithoutCategoryIds toDtoWithoutCategories(Book book);

    @AfterMapping
    default void setCategoryIds(@MappingTarget BookDto bookDto, Book book) {
        Set<Long> longSet = book.getCategories().stream()
                .map(c -> c.getId())
                .collect(Collectors.toSet());
        bookDto.setCategoryIds(longSet);
    }

    @Named("bookFromId")
    default Book bookFromId(Long id) {
        return null;
    }
}
