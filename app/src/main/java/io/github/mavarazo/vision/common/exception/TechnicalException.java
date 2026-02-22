package io.github.mavarazo.vision.common.exception;

import org.springframework.http.HttpStatus;

public class TechnicalException extends AbstractException {
    
    public TechnicalException(final HttpStatus status, final String errorCode, final Throwable cause) {
        super(status, errorCode, null, null);
        initCause(cause);
    }
}
