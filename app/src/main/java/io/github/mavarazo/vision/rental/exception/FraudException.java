package io.github.mavarazo.vision.rental.exception;

import io.github.mavarazo.vision.shared.exception.Alertable;
import io.github.mavarazo.vision.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class FraudException extends BusinessException implements Alertable {

    private FraudException(final String errorCode, final String message, final Map<String, Object> parameters) {
        super(HttpStatus.UNPROCESSABLE_CONTENT, errorCode, message, parameters);
    }

    public static FraudException ofUnusualLongRentalTime() {
        return new FraudException("rental.fraud-detection", "Fraud detection", null);
    }
}
