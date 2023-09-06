package bookstore.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bookstore.dto.book.BookDto;
import bookstore.dto.book.CreateBookRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.apache.commons.lang3.builder.EqualsBuilder;
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
class BookControllerTest {
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
                    new ClassPathResource("database/books/insert-test-books.sql")
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
                    new ClassPathResource("database/books/delete-all-test-books.sql")
            );

        }
    }

    @Test
    @WithMockUser(username = "user")
    @DisplayName("""
            Get all books
            """)
    void findAll_GivenBooksCatalog_ReturnsAllBooks() throws Exception {
        // Given
        List<BookDto> expected = new ArrayList<>();
        expected.add(new BookDto().setId(1L).setTitle("Title1").setAuthor("Author1")
                .setIsbn("isbn1").setPrice(BigDecimal.valueOf(1.99)).setDescription("Description1")
                .setCoverImage("CoverImage1"));
        expected.add(new BookDto().setId(2L).setTitle("Title2").setAuthor("Author2")
                .setIsbn("isbn2").setPrice(BigDecimal.valueOf(2.99)).setDescription("Description2")
                .setCoverImage("CoverImage2"));
        expected.add(new BookDto().setId(3L).setTitle("Title3").setAuthor("Author3")
                .setIsbn("isbn3").setPrice(BigDecimal.valueOf(3.99)).setDescription("Description3")
                .setCoverImage("CoverImage3"));

        // When
        MvcResult result = mockMvc.perform(get("/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        // Then
        BookDto[] actual = objectMapper.readValue(result.getResponse()
                .getContentAsByteArray(), BookDto[].class);
        assertEquals(3, actual.length);
        assertEquals(expected, Arrays.stream(actual).toList());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Sql(
            scripts = "classpath:database/books/delete-1-test-book.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("""
            Create a new Book
            """)
    void createBook_ValidCreateBookRequestDto_Success() throws Exception {
        // Given
        CreateBookRequestDto createBookRequestDto =
                new CreateBookRequestDto()
                        .setAuthor("Author-1")
                        .setTitle("Title-1")
                        .setIsbn("isbn-1")
                        .setPrice(BigDecimal.TEN)
                        .setDescription("Description-1")
                        .setCoverImage("CoverImage-1")
                        .setCategoryIds(new HashSet<>());

        BookDto expected =
                new BookDto()
                        .setAuthor(createBookRequestDto.getAuthor())
                        .setTitle(createBookRequestDto.getTitle())
                        .setIsbn(createBookRequestDto.getIsbn())
                        .setPrice(createBookRequestDto.getPrice())
                        .setDescription(createBookRequestDto.getDescription())
                        .setCoverImage(createBookRequestDto.getCoverImage())
                        .setCategoryIds(createBookRequestDto.getCategoryIds());

        String jsonRequest = objectMapper.writeValueAsString(createBookRequestDto);

        // When
        MvcResult result = mockMvc.perform(post("/books")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        BookDto actual =
                objectMapper.readValue(result.getResponse().getContentAsString(), BookDto.class);
        assertNotNull(actual);
        assertNotNull(actual.getId());
        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    @Test
    @WithMockUser(username = "user")
    @DisplayName("""
            Get book by id
            """)
    void getBookById_ValidId_Success() throws Exception {
        // Given
        Long bookId = 1L;
        BookDto expected =
                new BookDto()
                        .setId(bookId)
                        .setAuthor("Author1")
                        .setTitle("Title1")
                        .setIsbn("isbn1")
                        .setPrice(BigDecimal.valueOf(1.99))
                        .setDescription("Description1")
                        .setCoverImage("CoverImage1")
                        .setCategoryIds(new HashSet<>());

        // When
        MvcResult result = mockMvc.perform(get("/books/" + bookId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        BookDto actual =
                objectMapper.readValue(result.getResponse().getContentAsString(), BookDto.class);
        assertEquals(expected, actual);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Sql(
            scripts = "classpath:database/books/insert-1-test-book.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/books/delete-1-test-book.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("""
            Update book by id
            """)
    void update_ValidIdAndCreateBookRequestDto_Success() throws Exception {
        // Given
        long bookId = -4L;
        CreateBookRequestDto createBookRequestDto =
                new CreateBookRequestDto()
                        .setAuthor("Author-1")
                        .setTitle("Title-1")
                        .setIsbn("isbn-1")
                        .setPrice(BigDecimal.TEN)
                        .setDescription("Description-1")
                        .setCoverImage("CoverImage-1")
                        .setCategoryIds(new HashSet<>());

        BookDto expected =
                new BookDto()
                        .setAuthor(createBookRequestDto.getAuthor())
                        .setTitle(createBookRequestDto.getTitle())
                        .setIsbn(createBookRequestDto.getIsbn())
                        .setPrice(createBookRequestDto.getPrice())
                        .setDescription(createBookRequestDto.getDescription())
                        .setCoverImage(createBookRequestDto.getCoverImage())
                        .setCategoryIds(createBookRequestDto.getCategoryIds());

        String jsonRequest = objectMapper.writeValueAsString(createBookRequestDto);

        // When
        MvcResult result = mockMvc.perform(put("/books/" + bookId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        BookDto actual =
                objectMapper.readValue(result.getResponse().getContentAsString(), BookDto.class);
        assertEquals(expected, actual);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Sql(
            scripts = "classpath:database/books/insert-1-test-book.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @DisplayName("""
            Delete book by id
            """)
    void delete_ValidId_Success() throws Exception {
        // Given
        long bookId = -4L;

        // When
        mockMvc.perform(delete("/books/" + bookId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Test
    @WithMockUser(username = "user")
    @DisplayName("""
            Search books by criteria
            """)
    void searchBooks_GivenBooksCatalog_ReturnsBooksByCriteria() throws Exception {
        // Given
        List<BookDto> expected = new ArrayList<>();
        expected.add(new BookDto().setId(1L).setTitle("Title1").setAuthor("Author1")
                .setIsbn("isbn1").setPrice(BigDecimal.valueOf(1.99)).setDescription("Description1")
                .setCoverImage("CoverImage1"));

        // When
        MvcResult result = mockMvc.perform(get("/books/search?titles=Title1")
                        .contentType(MediaType.APPLICATION_JSON)
                        )
                .andExpect(status().isOk())
                .andReturn();

        // Then
        BookDto[] actual = objectMapper.readValue(result.getResponse()
                .getContentAsByteArray(), BookDto[].class);
        assertEquals(1, actual.length);
        assertEquals(expected, Arrays.stream(actual).toList());
    }
}
