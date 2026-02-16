package io.github.mavarazo.pocologgo.app.trace;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(2)
@Slf4j
public class AuthContextFilter extends AbstractContextFilter {

    private static final String USER_ID = "user.id";
    private static final String TYPE = "auth.type";

    @Override
    protected Map<String, String> getContext(final HttpServletRequest request, final HttpServletResponse response) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        final Map<String, String> result = new HashMap<>();
        result.put(USER_ID, "anonymous");
        result.put(TYPE, "unauthenticated");

        if (authHeader != null) {
            if (authHeader.startsWith("Bearer ")) {
                result.put(TYPE, "Bearer");
                result.put(USER_ID, "jwt-user-id");
            } else if (authHeader.startsWith("Basic ")) {
                result.put(TYPE, "Basic");
                result.put(USER_ID, extractUserId(authHeader));
            }
        }

        return result;
    }

    private static String extractUserId(final String base64) {
        try {
            final String decoded = new String(Base64.getDecoder().decode(base64));
            return decoded.split(":")[0];
        } catch (final Exception e) {
            return "invalid-basic";
        }
    }
}