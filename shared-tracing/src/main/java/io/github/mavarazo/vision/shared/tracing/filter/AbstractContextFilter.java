package io.github.mavarazo.vision.shared.tracing.filter;

import io.micrometer.common.KeyValue;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.filter.ServerHttpObservationFilter;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
public abstract class AbstractContextFilter extends OncePerRequestFilter {
    
    protected abstract Map<String, String> addLowCardinalityKeyValue(HttpServletRequest request, HttpServletResponse response);

    @Override
    public void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
        final Map<String, String> lowCardinalityKeyValues = addLowCardinalityKeyValue(request, response);

        ServerHttpObservationFilter.findObservationContext(request)
                .ifPresent(observationContext ->
                        lowCardinalityKeyValues.forEach((k, v) ->
                                observationContext.addLowCardinalityKeyValue(KeyValue.of(k, v))));

        filterChain.doFilter(request, response);
    }
}
