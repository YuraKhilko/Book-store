package bookstore.dto.book;

public record BookSearchParametersDto(String[] authors, String[] titles, String[] isbns,
                                      String[] prices, String[] categories) {
}
