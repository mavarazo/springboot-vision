package io.github.mavarazo.pocologgo.app.trace;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.spi.LoggingEventBuilder;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.stereotype.Component;

@Component
@Order
@Slf4j
public class RequestObservationContextLogger implements ObservationHandler<ServerRequestObservationContext> {

    private static final String START_TIME = "start";

    @Override
    public void onStart(final ServerRequestObservationContext context) {
        context.put(START_TIME, System.nanoTime());

        final HttpServletRequest request = context.getCarrier();

        final String method = request.getMethod();
        final String url = request.getRequestURI();
        final String queryString = request.getQueryString();

        final LoggingEventBuilder loggingEventBuilder = log.atInfo()
                .addKeyValue("http.method", method)
                .addKeyValue("http.query", queryString);

        context.getAllKeyValues()
                .forEach(keyValue -> loggingEventBuilder.addKeyValue(keyValue.getKey(), keyValue.getValue()));

        loggingEventBuilder.log("Incoming request: {} {}", method, url);
    }

    @Override
    public void onStop(final ServerRequestObservationContext context) {
        final Long startTime = context.get(START_TIME);
        final long durationMs = startTime != null ? (System.nanoTime() - startTime) / 1_000_000 : -1;

        final var request = context.getCarrier();
        final String method = request.getMethod();
        final String url = request.getRequestURI();

        final var response = context.getResponse();
        final int status = response != null ? response.getStatus() : -1;

        final boolean isError = status >= 400;
        final var logLevel = isError ? log.atWarn() : log.atInfo();

        final LoggingEventBuilder loggingEventBuilder = logLevel
                .addKeyValue("http.duration-ms", durationMs)
                .addKeyValue("http.status-code", status);

        context.getAllKeyValues()
                .forEach(keyValue -> loggingEventBuilder.addKeyValue(keyValue.getKey(), keyValue.getValue()));

        loggingEventBuilder.log("Completed request: {} {} -> {} in {}ms",
                method,
                url,
                status,
                durationMs);
    }

    @Override
    public boolean supportsContext(final Observation.Context context) {
        return context instanceof ServerRequestObservationContext;
    }
}
