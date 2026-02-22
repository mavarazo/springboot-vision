package io.github.mavarazo.vision.rental.exception;

import io.github.mavarazo.vision.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

public class NotFoundException extends BusinessException {

    private NotFoundException(final String errorCode, final String message, final Map<String, Object> parameters) {
        super(HttpStatus.UNPROCESSABLE_CONTENT, errorCode, message, parameters);
    }

    public static NotFoundException ofRental(final UUID id) {
        return new NotFoundException("rental.not-found", "Rental with ID '{}' not found", Map.of("id", id));
    }
}
