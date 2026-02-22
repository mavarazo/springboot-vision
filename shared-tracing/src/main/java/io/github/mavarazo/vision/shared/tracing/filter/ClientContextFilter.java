package io.github.mavarazo.vision.shared.tracing.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


@Component
@EnableConfigurationProperties(ClientContextProperties.class)
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class ClientContextFilter extends AbstractContextFilter {

    private final ClientContextProperties properties;

    @Override
    protected Map<String, String> addLowCardinalityKeyValue(final HttpServletRequest request, final HttpServletResponse response) {
        final Map<String, String> result = new HashMap<>();

        result.put("client.ip", request.getRemoteAddr());
        result.putAll(getClientInformation(request));

        return result;
    }

    private Map<String, String> getClientInformation(final HttpServletRequest request) {
        final List<String> headers = new ArrayList<>();
        headers.add(HttpHeaders.USER_AGENT);
        headers.addAll(properties.getHeaders());

        final Map<String, String> result = new TreeMap<>();
        headers.forEach(header -> result.put("header." + header.toLowerCase(), getHeader(header, request)));
        return result;
    }

    private String getHeader(final String header, final HttpServletRequest request) {
        if (HttpHeaders.AUTHORIZATION.equalsIgnoreCase(header)) {
            return "*****";
        }

        return request.getHeader(header);
    }
}
