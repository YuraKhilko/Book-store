package bookstore.controller;

import bookstore.dto.book.BookDto;
import bookstore.dto.book.CreateBookRequestDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

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
                    new ClassPathResource("database/books/delete-test-books.sql")
            );

        }
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("""
            Get all books
            """)
    void findAll() throws Exception {
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
        BookDto[] actual = objectMapper.readValue(result.getResponse().getContentAsByteArray(), BookDto[].class);
        assertEquals(3, actual.length);
        assertEquals(expected, Arrays.stream(actual).toList());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Sql(
            scripts = "classpath:database/books/delete-test-books.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("""
            Create a new Book
            """)
    void createBook_ValidCreateBookRequestDto_Success() throws Exception {
        // Given
        CreateBookRequestDto createBookRequestDto = getCreateBookRequestDto();
        BookDto expected = getBookDtoByCreateBookRequestDto(createBookRequestDto);
        String jsonRequest = objectMapper.writeValueAsString(createBookRequestDto);

        // When
        MvcResult result = mockMvc.perform(post("/books")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        BookDto actual = objectMapper.readValue(result.getResponse().getContentAsString(), BookDto.class);
        assertNotNull(actual);
        assertNotNull(actual.getId());
        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    @Test
    void getBookById() {
    }

    @Test
    void update() {
    }

    @Test
    void delete() {
    }

    @Test
    void searchBooks() {
    }

    private CreateBookRequestDto getCreateBookRequestDto() {
        CreateBookRequestDto createBookRequestDto = new CreateBookRequestDto();
        createBookRequestDto.setAuthor("Author-1");
        createBookRequestDto.setTitle("Title-1");
        createBookRequestDto.setIsbn("isbn-1");
        createBookRequestDto.setPrice(BigDecimal.TEN);
        createBookRequestDto.setDescription("Description-1");
        createBookRequestDto.setCoverImage("CoverImage-1");
        createBookRequestDto.setCategoryIds(new HashSet<>());
        return createBookRequestDto;
    }

    private BookDto getBookDtoByCreateBookRequestDto(CreateBookRequestDto createBookRequestDto) {
        BookDto bookDto = new BookDto();
        bookDto.setAuthor(createBookRequestDto.getAuthor());
        bookDto.setTitle(createBookRequestDto.getTitle());
        bookDto.setIsbn(createBookRequestDto.getIsbn());
        bookDto.setPrice(createBookRequestDto.getPrice());
        bookDto.setDescription(createBookRequestDto.getDescription());
        bookDto.setCoverImage(createBookRequestDto.getCoverImage());
        bookDto.setCategoryIds(createBookRequestDto.getCategoryIds());
        return bookDto;
    }
}