package se.ifmo.ru.dto.response;

public record ErrorResponse(String title, int status, String detail) {
}
