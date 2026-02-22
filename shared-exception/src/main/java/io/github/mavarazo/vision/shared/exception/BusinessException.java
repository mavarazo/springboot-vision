package io.github.mavarazo.vision.shared.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public abstract class BusinessException extends AbstractException {

    public BusinessException(final HttpStatus status, final String errorCode, final String message, final Map<String, Object> parameters) {
        super(status, errorCode, message, parameters);
    }
}
