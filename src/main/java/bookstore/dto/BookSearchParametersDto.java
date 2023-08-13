package bookstore.dto;

public record BookSearchParametersDto(String[] authors, String[] titles, String[] isbns,
                                      String[] prices, String[] descriptions,
                                      String[] coverImages) {
}
