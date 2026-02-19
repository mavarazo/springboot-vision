package io.github.mavarazo.pocologgo.app.trace;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order
public class MdcObservationHandler implements ObservationHandler<Observation.Context> {

    @Override
    public void onScopeOpened(final Observation.Context context) {
        context.getAllKeyValues().forEach(kv ->
                MDC.put(normalize(kv.getKey()), kv.getValue()));
    }

    @Override
    public void onScopeClosed(final Observation.Context context) {
        context.getAllKeyValues().forEach(kv ->
                MDC.remove(normalize(kv.getKey())));
    }

    private String normalize(final String key) {
        return key.toLowerCase().replace("-", ".");
    }

    @Override
    public boolean supportsContext(final Observation.Context context) {
        return true;
    }
}
