package io.github.mavarazo.pocologgo.app.trace;

import io.micrometer.common.KeyValue;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.filter.ServerHttpObservationFilter;

import java.io.IOException;
import java.util.Map;

public abstract class AbstractContextFilter extends OncePerRequestFilter {

    protected abstract Map<String, String> getContext(HttpServletRequest request, HttpServletResponse response);

    @Override
    public void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
        final Map<String, String> context = getContext(request, response);

        ServerHttpObservationFilter.findObservationContext(request)
                .ifPresent(observationContext -> {
                    context.forEach((k, v) -> {
                        observationContext.addHighCardinalityKeyValue(KeyValue.of(k, v));
                    });
                });

        filterChain.doFilter(request, response);
    }
}
