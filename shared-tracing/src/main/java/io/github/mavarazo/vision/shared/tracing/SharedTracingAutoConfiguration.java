package io.github.mavarazo.vision.shared.tracing;

import io.github.mavarazo.vision.shared.tracing.filter.AuthContextFilter;
import io.github.mavarazo.vision.shared.tracing.filter.ClientContextFilter;
import io.github.mavarazo.vision.shared.tracing.filter.ClientContextProperties;
import io.github.mavarazo.vision.shared.tracing.filter.CorrelationIdContextFilter;
import io.github.mavarazo.vision.shared.tracing.handler.MdcObservationHandler;
import io.github.mavarazo.vision.shared.tracing.handler.RequestObservationContextLogger;
import io.micrometer.tracing.Tracer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@EnableConfigurationProperties(ClientContextProperties.class)
@Import(ObservationConfig.class)
public class SharedTracingAutoConfiguration {

    @Bean
    public AuthContextFilter authContextFilter() {
        return new AuthContextFilter();
    }

    @Bean
    public ClientContextFilter clientContextFilter(final ClientContextProperties properties) {
        return new ClientContextFilter(properties);
    }

    @Bean
    public CorrelationIdContextFilter correlationIdContextFilter(final Tracer tracer) {
        return new CorrelationIdContextFilter(tracer);
    }

    @Bean
    public MdcObservationHandler mdcObservationHandler() {
        return new MdcObservationHandler();
    }

    @Bean
    public RequestObservationContextLogger requestObservationContextLogger() {
        return new RequestObservationContextLogger();
    }
}
