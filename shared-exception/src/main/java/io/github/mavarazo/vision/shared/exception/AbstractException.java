package io.github.mavarazo.vision.shared.exception;

import lombok.Getter;
import org.springframework.core.NestedRuntimeException;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public abstract class AbstractException extends NestedRuntimeException {

    private final HttpStatus status;
    private final String errorCode;
    private final String message;
    private final Map<String, Object> parameters;

    public AbstractException(final HttpStatus status, final String errorCode, final String message, final Map<String, Object> parameters) {
        super(errorCode);
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.parameters = parameters;
    }
}
