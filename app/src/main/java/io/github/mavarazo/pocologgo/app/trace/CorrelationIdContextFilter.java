package io.github.mavarazo.pocologgo.app.trace;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Component
@Order(1)
public class CorrelationIdContextFilter extends AbstractContextFilter {

    private static final String X_CORRELATION_ID = "X-Correlation-ID";

    @Override
    protected Map<String, String> getContext(final HttpServletRequest request, final HttpServletResponse response) {
        final String correlationId = Objects.requireNonNullElse(request.getHeader(X_CORRELATION_ID), UUID.randomUUID().toString());
        response.setHeader(X_CORRELATION_ID, correlationId);
        return Map.of("correlation.id", correlationId);
    }
}
