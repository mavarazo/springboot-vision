package io.github.mavarazo.vision.user.exception;

import io.github.mavarazo.vision.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class NotFoundException extends BusinessException {

    private NotFoundException(final String errorCode, final String message, final Map<String, Object> parameters) {
        super(HttpStatus.NOT_FOUND, errorCode, message, parameters);
    }

    public static NotFoundException ofUserBySnn(final String ssn) {
        return new NotFoundException("user.not-found", "User with SSN {} not found", Map.of("ssn", ssn));
    }
}
