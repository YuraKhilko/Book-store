package bookstore.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import bookstore.dto.book.BookDto;
import bookstore.dto.book.BookDtoWithoutCategoryIds;
import bookstore.dto.book.BookSearchParametersDto;
import bookstore.dto.book.CreateBookRequestDto;
import bookstore.exception.EntityNotFoundException;
import bookstore.mapper.BookMapper;
import bookstore.model.Book;
import bookstore.model.Category;
import bookstore.repository.CategoryRepository;
import bookstore.repository.book.BookRepository;
import bookstore.repository.book.BookSpecificationBuilderImpl;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {
    @InjectMocks
    private BookServiceImpl bookServiceImpl;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private BookMapper bookMapper;
    @Mock
    private BookSpecificationBuilderImpl bookSpecificationBuilder;

    @Test
    @DisplayName("""
            Verify save() method works
            """)
    void save_ValidCreateBookRequestDto_ReturnsBookDto() {
        // Given
        CreateBookRequestDto createBookRequestDto = getCreateBookRequestDto();

        Book book = getBookByCreateBookRequestDto(createBookRequestDto);

        BookDto expected = getBookDtoByBook(book);
        expected.setId(1L);

        Mockito.when(bookMapper.toEntity(createBookRequestDto)).thenReturn(book);
        Mockito.when(bookRepository.save(book)).thenReturn(book);
        Mockito.when(bookMapper.toDto(book)).thenReturn(expected);

        // When
        BookDto actual = bookServiceImpl.save(createBookRequestDto);

        // Then
        assertThat(actual).isEqualTo(expected);
        verify(bookRepository, times(1)).save(book);
        verify(bookMapper, times(1)).toEntity(createBookRequestDto);
        verify(bookMapper, times(1)).toDto(book);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("""
            Verify the List of all BookDto was returned
            """)
    void findAll_ValidPageable_ReturnAllBooks() {
        // Given
        Book book = getBook();
        BookDto expected = getBookDtoByBook(book);

        Pageable pageable = PageRequest.of(0, 10);
        List<Book> books = List.of(book);
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());

        Mockito.when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        Mockito.when(bookMapper.toDto(book)).thenReturn(expected);

        // When
        List<BookDto> actual = bookServiceImpl.findAll(pageable);

        // Then
        assertThat(actual).hasSize(1);
        assertThat(actual.get(0)).isEqualTo(expected);
        verify(bookRepository, times(1)).findAll(pageable);
        verify(bookMapper, times(1)).toDto(book);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("""
            Verify the correct BookDto was returned if it exists
            """)
    void findById_WithValidBookId_ReturnsValidBookDto() {
        // Given
        Book book = getBook();
        Long bookId = book.getId();
        BookDto expected = getBookDtoByBook(book);

        Mockito.when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));
        Mockito.when(bookMapper.toDto(book)).thenReturn(expected);

        // When
        BookDto actual = bookServiceImpl.findById(bookId);

        // Then
        assertEquals(expected, actual);
        verify(bookRepository, times(1)).findById(anyLong());
        verify(bookMapper, times(1)).toDto(book);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("""
            Verify the EntityNotFoundException was thrown for invalid bookId
            """)
    void findById_WithInvalidBookId_ThrowEntityNotFoundException() {
        // Given
        Long bookId = -1L;
        Mockito.when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // When
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> bookServiceImpl.findById(bookId));

        // Then
        String expected = "Can't find book by id " + bookId;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verify(bookRepository, times(1)).findById(bookId);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("""
            Verify deleteById() method works
            """)
    void deleteById_ValidId_DoesNotThrowException() {
        // Then
        assertDoesNotThrow(() -> bookServiceImpl.deleteById(anyLong()));
    }

    @Test
    @DisplayName("""
            Verify update() method works
            """)
    void update_ValidBook_BookHasBeenUpdated() {
        // Given
        Long bookId = 1L;
        CreateBookRequestDto createBookRequestDto = getCreateBookRequestDto();

        Book book = getBookByCreateBookRequestDto(createBookRequestDto);

        BookDto expected = getBookDtoByBook(book);
        expected.setId(bookId);

        Mockito.when(bookMapper.toEntity(createBookRequestDto)).thenReturn(book);
        Mockito.when(bookRepository.save(book)).thenReturn(book);
        Mockito.when(bookMapper.toDto(book)).thenReturn(expected);

        // When
        BookDto actual = bookServiceImpl.update(bookId, createBookRequestDto);

        // Then
        assertThat(actual).isEqualTo(expected);
        verify(bookRepository, times(1)).save(book);
        verify(bookMapper, times(1)).toDto(book);
        verify(bookMapper, times(1)).toEntity(createBookRequestDto);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("""
            Verify search() method works
            """)
    void search_ValidBookSearchParametersDtoAndPageable_ReturnAllBooks() {
        // Given
        String[] emptyArray = new String[0];
        Book book = getBook();
        BookDto expected = getBookDtoByBook(book);
        Specification<Book> spec = Specification.where(null);
        BookSearchParametersDto bookSearchParametersDto =
                new BookSearchParametersDto(emptyArray, emptyArray, emptyArray, emptyArray,
                        emptyArray);

        Pageable pageable = PageRequest.of(0, 10);
        List<Book> books = List.of(book);
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());

        Mockito.when(bookSpecificationBuilder.build(bookSearchParametersDto)).thenReturn(spec);
        Mockito.when(bookRepository.findAll(spec, pageable)).thenReturn(bookPage);
        Mockito.when(bookMapper.toDto(book)).thenReturn(expected);

        // When
        List<BookDto> actual = bookServiceImpl.search(bookSearchParametersDto, pageable);

        // Then
        assertThat(actual).hasSize(1);
        assertThat(actual.get(0)).isEqualTo(expected);
        verify(bookRepository, times(1)).findAll(spec, pageable);
        verify(bookMapper, times(1)).toDto(book);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("""
            Verify findByCategoryId() method works and returns books by categoryId
            """)
    void findByCategoryId_WithValidCategoryId_ReturnsAllBooksWithSuchCategory() {
        // Given
        Category category = getCategory();
        Book book = getBook();
        category.setBooks(Set.of(book));
        BookDtoWithoutCategoryIds expected =
                getBookDtoWithoutCategoryIdsFromBook(book);
        Mockito.when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        Mockito.when(bookMapper.toDtoWithoutCategories(book)).thenReturn(expected);

        // When
        List<BookDtoWithoutCategoryIds> actual =
                bookServiceImpl.findByCategoryId(anyLong());
        // Then
        assertThat(actual).hasSize(1);
        assertThat(actual.get(0)).isEqualTo(expected);
        verify(categoryRepository, times(1)).findById(anyLong());
        verify(bookMapper, times(1)).toDtoWithoutCategories(book);
        verifyNoMoreInteractions(categoryRepository, bookMapper);
    }

    @Test
    @DisplayName("""
            Verify the EntityNotFoundException was thrown for invalid categoryId
            """)
    void findByCategoryId_WithInvalidCategoryId_ThrowEntityNotFoundException() {
        // Given
        Long categoryId = -1L;
        Mockito.when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class,
                        () -> bookServiceImpl.findByCategoryId(categoryId));

        // Then
        String expected = "Can't find Category by id " + categoryId;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verify(categoryRepository, times(1)).findById(categoryId);
        verifyNoMoreInteractions(categoryRepository);
    }

    private Book getBook() {
        Book book = new Book();
        book.setId(1L);
        book.setAuthor("Author1");
        book.setTitle("Title1");
        book.setIsbn("isbn1");
        book.setPrice(BigDecimal.TEN);
        book.setDescription("Description1");
        book.setCoverImage("CoverImage1");
        book.setCategories(new HashSet<>());
        return book;
    }

    private BookDto getBookDtoByBook(Book book) {
        BookDto bookDto = new BookDto();
        bookDto.setId(book.getId());
        bookDto.setAuthor(book.getAuthor());
        bookDto.setTitle(book.getTitle());
        bookDto.setIsbn(book.getIsbn());
        bookDto.setPrice(book.getPrice());
        bookDto.setDescription(book.getDescription());
        bookDto.setCoverImage(book.getCoverImage());
        bookDto.setCategoryIds(book.getCategories().stream()
                .map(Category::getId)
                .collect(Collectors.toSet()));
        return bookDto;
    }

    private Book getBookByCreateBookRequestDto(CreateBookRequestDto createBookRequestDto) {
        Book book = new Book();
        book.setAuthor(createBookRequestDto.getAuthor());
        book.setTitle(createBookRequestDto.getTitle());
        book.setIsbn(createBookRequestDto.getIsbn());
        book.setPrice(createBookRequestDto.getPrice());
        book.setDescription(createBookRequestDto.getDescription());
        book.setCoverImage(createBookRequestDto.getCoverImage());
        book.setCategories(createBookRequestDto.getCategoryIds().stream()
                .map(l -> {
                    Category category = new Category();
                    category.setId(l);
                    return category;
                })
                .collect(Collectors.toSet()));
        return book;
    }

    private CreateBookRequestDto getCreateBookRequestDto() {
        CreateBookRequestDto createBookRequestDto = new CreateBookRequestDto();
        createBookRequestDto.setAuthor("Author1");
        createBookRequestDto.setTitle("Title1");
        createBookRequestDto.setIsbn("isbn1");
        createBookRequestDto.setPrice(BigDecimal.TEN);
        createBookRequestDto.setDescription("Description1");
        createBookRequestDto.setCoverImage("CoverImage1");
        createBookRequestDto.setCategoryIds(new HashSet<>());
        return createBookRequestDto;
    }

    private Category getCategory() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Name");
        category.setDescription("Description");
        return category;
    }

    private BookDtoWithoutCategoryIds getBookDtoWithoutCategoryIdsFromBook(Book book) {
        BookDtoWithoutCategoryIds bookDtoWithoutCategoryIds = new BookDtoWithoutCategoryIds();
        bookDtoWithoutCategoryIds.setAuthor(book.getAuthor());
        bookDtoWithoutCategoryIds.setTitle(book.getTitle());
        bookDtoWithoutCategoryIds.setIsbn(book.getIsbn());
        bookDtoWithoutCategoryIds.setPrice(book.getPrice());
        bookDtoWithoutCategoryIds.setDescription(book.getDescription());
        bookDtoWithoutCategoryIds.setCoverImage(book.getCoverImage());
        bookDtoWithoutCategoryIds.setId(book.getId());
        return bookDtoWithoutCategoryIds;
    }
}
