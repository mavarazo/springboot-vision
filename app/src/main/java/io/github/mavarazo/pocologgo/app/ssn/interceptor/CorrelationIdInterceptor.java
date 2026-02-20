package io.github.mavarazo.pocologgo.app.ssn.interceptor;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.ObservationView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class CorrelationIdInterceptor implements ClientHttpRequestInterceptor {

    private final ObservationRegistry observationRegistry;

    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution) throws IOException {
        final String correlationId = extractCorrelationId();
        if (StringUtils.hasText(correlationId)) {
            request.getHeaders().add("X-Correlation-ID", correlationId);
            log.debug("Added correlation ID to outgoing request: {}", correlationId);
        } else {
            log.debug("No correlation ID found in observation context");
        }

        return execution.execute(request, body);
    }

    private String extractCorrelationId() {
        final Observation currentObservation = observationRegistry.getCurrentObservation();
        if (currentObservation == null) {
            return null;
        }

        ObservationView view = currentObservation;
        while (view != null) {
            for (final KeyValue keyValue : view.getContextView().getAllKeyValues()) {
                if (keyValue.getKey().equals("correlation.id")) {
                    return keyValue.getValue();
                }
            }
            view = view.getContextView().getParentObservation();
        }

        return null;
    }
}
