package io.github.mavarazo.vision.rental.exception;

import io.github.mavarazo.vision.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

public class NotFoundException extends BusinessException {

    private NotFoundException(final String errorCode, final String message, final Map<String, Object> parameters) {
        super(HttpStatus.NOT_FOUND, errorCode, message, parameters);
    }

    public static NotFoundException ofInsurance(final UUID insuranceId) {
        return new NotFoundException("insurance.not-found", "Insurance with ID '{}' not found", Map.of("insuranceId", insuranceId));
    }
}
