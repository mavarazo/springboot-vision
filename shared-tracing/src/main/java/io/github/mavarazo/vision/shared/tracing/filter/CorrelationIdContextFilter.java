package io.github.mavarazo.vision.shared.tracing.filter;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Order(1)
public class CorrelationIdContextFilter extends OncePerRequestFilter {

    private final Tracer tracer;
    
    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
        final String correlationId = Objects.requireNonNullElse(request.getHeader("x-correlation-id"), UUID.randomUUID().toString());
        if (StringUtils.hasText(correlationId)) {
            tracer.getBaggage("correlation.id").makeCurrent(correlationId);
            response.setHeader("x-correlation-id", correlationId);
        }

        filterChain.doFilter(request, response);
    }
}
