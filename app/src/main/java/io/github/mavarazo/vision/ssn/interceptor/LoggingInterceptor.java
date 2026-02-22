package io.github.mavarazo.vision.ssn.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;

@Slf4j
public class LoggingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution) throws IOException {
        final HttpMethod method = request.getMethod();
        final URI uri = request.getURI();

        log.atDebug()
                .log("Outgoing request: {} {}",
                        method,
                        uri);

        return execution.execute(request, body);
    }
}
