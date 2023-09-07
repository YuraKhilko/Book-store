package bookstore.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bookstore.dto.book.BookDtoWithoutCategoryIds;
import bookstore.dto.category.CategoryDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CategoryControllerTest {
    protected static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(
            @Autowired DataSource dataSource,
            @Autowired WebApplicationContext applicationContext
    ) throws SQLException {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/categories/insert-test-categories.sql")
            );

        }
    }

    @AfterAll
    static void afterAll(
            @Autowired DataSource dataSource
    ) {
        teardown(dataSource);
    }

    @SneakyThrows
    private static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/categories/delete-all-test-categories.sql")
            );
        }
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Sql(
            scripts = "classpath:database/categories/delete-1-test-category.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("""
            Create a new Category
            """)
    void createCategory_ValidCategoryDto_Success() throws Exception {
        // Given
        CategoryDto expected = new CategoryDto()
                .setName("Name_Delete1")
                .setDescription("Description1");

        String jsonRequest = objectMapper.writeValueAsString(expected);

        // When
        MvcResult result = mockMvc.perform(post("/categories")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        CategoryDto actual =
                objectMapper.readValue(result.getResponse().getContentAsString(),
                        CategoryDto.class);

        assertEquals(expected, actual);
    }

    @Test
    @WithMockUser(username = "user")
    @DisplayName("""
            Get all categories
            """)
    void getAll_GivenCategoriesCatalog_ReturnsAllCategories() throws Exception {
        // Given
        List<CategoryDto> expected = new ArrayList<>();
        expected.add(new CategoryDto().setName("Name1").setDescription("Description1"));
        expected.add(new CategoryDto().setName("Name2").setDescription("Description2"));
        expected.add(new CategoryDto().setName("Name3").setDescription("Description3"));

        // When
        MvcResult result = mockMvc.perform(get("/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        CategoryDto[] actual = objectMapper.readValue(result.getResponse()
                .getContentAsByteArray(), CategoryDto[].class);
        assertEquals(3, actual.length);
        assertEquals(expected, Arrays.stream(actual).toList());
    }

    @Test
    @WithMockUser(username = "user")
    @DisplayName("""
            Get category by id
            """)
    void getCategoryById_ValidId_Success() throws Exception {
        // Given
        long categoryId = 1L;
        CategoryDto expected =
                new CategoryDto().setName("Name1").setDescription("Description1");

        // When
        MvcResult result = mockMvc.perform(get("/categories/" + categoryId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        CategoryDto actual =
                objectMapper.readValue(result.getResponse().getContentAsString(),
                        CategoryDto.class);
        assertEquals(expected, actual);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Sql(
            scripts = "classpath:database/categories/insert-1-test-category.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/categories/delete-1-test-category.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("""
            Update category by id
            """)
    void updateCategory_ValidIdAndCategoryDto_Success() throws Exception {
        // Given
        long categoryId = -4L;
        CategoryDto expected = new CategoryDto()
                .setName("Name_Delete1_changed")
                .setDescription("Description1");

        String jsonRequest = objectMapper.writeValueAsString(expected);

        // When
        MvcResult result = mockMvc.perform(put("/categories/" + categoryId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        CategoryDto actual =
                objectMapper.readValue(result.getResponse().getContentAsString(),
                        CategoryDto.class);

        assertEquals(expected, actual);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Sql(
            scripts = "classpath:database/categories/insert-1-test-category.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/categories/delete-1-test-category.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("""
            Delete category by id
            """)
    void deleteCategory() throws Exception {
        // Given
        long categoryId = -4L;

        // When
        mockMvc.perform(delete("/categories/" + categoryId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Test
    @WithMockUser(username = "user")
    @Sql(
            scripts = {"classpath:database/books/insert-test-books.sql",
                    "classpath:database/books_categories/insert-test-books-categories.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = {"classpath:database/books_categories/delete-all-test-books-categories.sql",
                    "classpath:database/books/delete-all-test-books.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("""
            Get all books with specific category
            """)
    void getBooksByCategoryId_GivenCategoriesCatalog_ReturnsAllBooksViaCategory() throws Exception {
        // Given
        long categoryId = 1L;
        List<BookDtoWithoutCategoryIds> expected = new ArrayList<>();
        expected.add(new BookDtoWithoutCategoryIds()
                .setId(1L)
                .setTitle("Title1")
                .setAuthor("Author1")
                .setIsbn("isbn1")
                .setPrice(BigDecimal.valueOf(1.99))
                .setDescription("Description1")
                .setCoverImage("CoverImage1"));
        expected.add(new BookDtoWithoutCategoryIds()
                .setId(2L).setTitle("Title2")
                .setAuthor("Author2")
                .setIsbn("isbn2")
                .setPrice(BigDecimal.valueOf(2.99))
                .setDescription("Description2")
                .setCoverImage("CoverImage2"));

        // When
        MvcResult result = mockMvc.perform(get("/categories/" + categoryId + "/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        BookDtoWithoutCategoryIds[] actual = objectMapper.readValue(result.getResponse()
                .getContentAsByteArray(), BookDtoWithoutCategoryIds[].class);
        assertEquals(2, actual.length);
        assertEquals(expected, Arrays.stream(actual).toList());
    }
}
