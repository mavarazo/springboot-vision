package io.github.mavarazo.vision.rental.exception;

import io.github.mavarazo.vision.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

public class UnprocessableContentException extends BusinessException {

    private UnprocessableContentException(final String errorCode, final String message, final Map<String, Object> parameters) {
        super(HttpStatus.UNPROCESSABLE_CONTENT, errorCode, message, parameters);
    }

    public static UnprocessableContentException ofInsurance(final UUID insuranceId) {
        return new UnprocessableContentException("insurance.not-found", "Insurance with ID '{}' not found", Map.of("insuranceId", insuranceId));
    }
}
