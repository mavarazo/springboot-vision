package io.github.mavarazo.pocologgo.app.trace;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.stereotype.Component;

@Component
@Order(3)
@RequiredArgsConstructor
public class CorrelationIdFilter implements ObservationFilter {

    @Override
    public Observation.Context map(final Observation.Context context) {
        if (context instanceof final ServerRequestObservationContext serverContext) {
            final String correlationId = serverContext.getCarrier().getHeader("X-Correlation-ID");
            if (correlationId != null) {
                context.addHighCardinalityKeyValue(KeyValue.of("correlation.id", correlationId));
            }
        }
        return context;
    }
}
