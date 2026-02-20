package io.github.mavarazo.pocologgo.app.trace;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Component
@Order(1)
public class CorrelationIdContextFilter extends AbstractContextFilter {

    @Override
    protected Map<String, String> getContext(final HttpServletRequest request, final HttpServletResponse response) {
        final Map<String, String> result = new HashMap<>();

        final String correlationId = Objects.requireNonNullElse(request.getHeader("X-Correlation-ID"), UUID.randomUUID().toString());
        if (StringUtils.hasText(correlationId)) {
            result.put("correlation.id", correlationId);
            response.setHeader("X-Correlation-ID", correlationId);
        }

        return result;
    }
}
