package io.github.mavarazo.vision.ssn;

import io.github.mavarazo.vision.ssn.interceptor.CorrelationIdInterceptor;
import io.github.mavarazo.vision.ssn.interceptor.LoggingInterceptor;
import io.github.mavarazo.vision.ssn.service.SocialInsuranceAdapter;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.List;

@Configuration
@Slf4j
public class SocialInsuranceAdapterConfig {

    @Value("${vision.social-insurance.endpoint}")
    private String endpoint;

    @Bean
    public SocialInsuranceAdapter socialInsuranceClient(final Tracer tracer, final ObservationRegistry observationRegistry) {
        final RestClient restClient = RestClient.builder()
                .baseUrl(endpoint)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (req, res) -> {
                    log.info("Client error occurred for request {}: {} {}", req.getURI(), res.getStatusCode(), res.getStatusText());
                    throw new HttpClientErrorException(res.getStatusCode(), res.getStatusText());
                })
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, (req, res) -> {
                    log.warn("Server error occurred for request {}: {} {}", req.getURI(), res.getStatusCode(), res.getStatusText());
                    throw new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE, res.getStatusText());
                })
                .requestInterceptors(interceptors -> interceptors.addAll(List.of(
                        new CorrelationIdInterceptor(tracer),
                        new LoggingInterceptor()
                )))
                .observationRegistry(observationRegistry)
                .build();

        final RestClientAdapter adapter = RestClientAdapter.create(restClient);
        final HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(SocialInsuranceAdapter.class);
    }
}
