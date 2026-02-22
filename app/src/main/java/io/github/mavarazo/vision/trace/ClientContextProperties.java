package io.github.mavarazo.vision.trace;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "web.logging.client-context")
public class ClientContextProperties {

    private final boolean enabled = false;
    private final List<String> headers = new ArrayList<>();
}
