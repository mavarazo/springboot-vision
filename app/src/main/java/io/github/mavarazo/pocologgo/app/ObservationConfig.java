package io.github.mavarazo.pocologgo.app;

import brave.baggage.BaggageFields;
import brave.baggage.CorrelationScopeConfig;
import brave.context.slf4j.MDCScopeDecorator;
import brave.propagation.CurrentTraceContextCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservationConfig {

    @Bean
    public CurrentTraceContextCustomizer mdcKeyCustomizer() {
        return builder -> builder.addScopeDecorator(MDCScopeDecorator.newBuilder()
                .clear()
                .add(CorrelationScopeConfig.SingleCorrelationField.newBuilder(BaggageFields.TRACE_ID)
                        .name("trace.id").build())
                .add(CorrelationScopeConfig.SingleCorrelationField.newBuilder(BaggageFields.SPAN_ID)
                        .name("span.id").build())
                .build());
    }
}
