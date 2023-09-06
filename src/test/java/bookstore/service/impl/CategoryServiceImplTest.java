package bookstore.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import bookstore.dto.category.CategoryDto;
import bookstore.exception.EntityNotFoundException;
import bookstore.mapper.CategoryMapper;
import bookstore.model.Category;
import bookstore.repository.CategoryRepository;
import java.util.List;
import java.util.Optional;
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

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    @InjectMocks
    private CategoryServiceImpl categoryServiceImpl;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryMapper categoryMapper;

    @Test
    @DisplayName("""
            Verify save() method works
            """)
    void save_ValidCategoryRequestDto_ReturnsCategoryDto() {
        // Given
        CategoryDto categoryRequestDto = getCategoryDto();

        Category category = getCategoryByCategoryDto(categoryRequestDto);

        CategoryDto expected = getCategoryDtoByCategory(category);

        Mockito.when(categoryMapper.toEntity(categoryRequestDto)).thenReturn(category);
        Mockito.when(categoryRepository.save(category)).thenReturn(category);
        Mockito.when(categoryMapper.toDto(category)).thenReturn(expected);

        // When
        CategoryDto actual = categoryServiceImpl.save(categoryRequestDto);

        // Then
        assertThat(actual).isEqualTo(expected);
        verify(categoryRepository, times(1)).save(category);
        verify(categoryMapper, times(1)).toEntity(categoryRequestDto);
        verify(categoryMapper, times(1)).toDto(category);
        verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("""
            Verify the correct CategoryDto was returned if it exists
            """)
    void findById_WithValidCategoryId_ReturnsValidCategoryDto() {
        // Given
        Long categoryId = 1L;
        Category category = getCategory();
        CategoryDto expected = getCategoryDtoByCategory(category);

        Mockito.when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        Mockito.when(categoryMapper.toDto(category)).thenReturn(expected);

        // When
        CategoryDto actual = categoryServiceImpl.findById(categoryId);

        // Then
        assertEquals(expected, actual);
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryMapper, times(1)).toDto(category);
        verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("""
            Verify the EntityNotFoundException was thrown for invalid categoryId
            """)
    void findById_WithInvalidCategoryId_ThrowEntityNotFoundException() {
        // Given
        Long categoryId = -1L;
        Mockito.when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class,
                        () -> categoryServiceImpl.findById(categoryId));

        // Then
        String expected = "Can't find Category by id " + categoryId;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verify(categoryRepository, times(1)).findById(categoryId);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("""
            Verify the List of all CategoryDto was returned
            """)
    void findAll_ValidPageable_ReturnAllCategories() {
        // Given
        Category category = getCategory();
        CategoryDto expected = getCategoryDtoByCategory(category);

        Pageable pageable = PageRequest.of(0, 10);
        List<Category> categories = List.of(category);
        Page<Category> categoryPage = new PageImpl<>(categories, pageable, categories.size());

        Mockito.when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);
        Mockito.when(categoryMapper.toDto(category)).thenReturn(expected);

        // When
        List<CategoryDto> actual = categoryServiceImpl.findAll(pageable);

        // Then
        assertThat(actual).hasSize(1);
        assertThat(actual.get(0)).isEqualTo(expected);
        verify(categoryRepository, times(1)).findAll(pageable);
        verify(categoryMapper, times(1)).toDto(category);
        verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("""
            Verify update() method works
            """)
    void update_ValidBook_BookHasBeenUpdated() {
        // Given
        CategoryDto categoryRequestDto = getCategoryDto();

        Category category = getCategoryByCategoryDto(categoryRequestDto);

        CategoryDto expectedCategoryDto = getCategoryDtoByCategory(category);

        Mockito.when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        Mockito.when(categoryRepository.save(category)).thenReturn(category);
        Mockito.when(categoryMapper.toDto(category)).thenReturn(expectedCategoryDto);

        // When
        CategoryDto actualCategoryDto = categoryServiceImpl.update(anyLong(), categoryRequestDto);

        // Then
        assertThat(actualCategoryDto).isEqualTo(expectedCategoryDto);
        verify(categoryRepository, times(1)).findById(anyLong());
        verify(categoryRepository, times(1)).save(category);
        verify(categoryMapper, times(1)).toDto(category);
        verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("""
            Verify deleteById() method works
            """)
    void deleteById_ValidId_DoesNotThrowException() {
        // Then
        assertDoesNotThrow(() -> categoryServiceImpl.deleteById(anyLong()));
    }

    private CategoryDto getCategoryDtoByCategory(Category category) {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName(category.getName());
        categoryDto.setDescription(category.getDescription());
        return categoryDto;
    }

    private Category getCategoryByCategoryDto(CategoryDto categoryDto) {
        Category category = new Category();
        category.setName(categoryDto.getName());
        category.setDescription(categoryDto.getDescription());
        return category;
    }

    private CategoryDto getCategoryDto() {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("Name");
        categoryDto.setDescription("Description");
        return categoryDto;
    }

    private Category getCategory() {
        Category category = new Category();
        category.setId(1L);
        category.setDescription("Description");
        category.setName("Name");
        return category;
    }
}
