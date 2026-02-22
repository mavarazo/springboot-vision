package io.github.mavarazo.vision.insurance.interceptor;

import io.micrometer.tracing.Baggage;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class CorrelationIdInterceptor implements ClientHttpRequestInterceptor {

    private final Tracer tracer;

    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution) throws IOException {
        final Baggage baggage = tracer.getBaggage("correlation.id");
        if (baggage.get() != null) {
            request.getHeaders().add("x-correlation-id", baggage.get());
        }
        return execution.execute(request, body);
    }
}
