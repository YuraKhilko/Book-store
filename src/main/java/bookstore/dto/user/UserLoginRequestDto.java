package bookstore.dto.user;

import bookstore.validation.Email;
import jakarta.validation.constraints.NotBlank;

public record UserLoginRequestDto(
        @Email
        String email,
        @NotBlank
        String password) {
}
