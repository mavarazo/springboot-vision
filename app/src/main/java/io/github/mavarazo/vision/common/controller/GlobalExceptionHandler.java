package io.github.mavarazo.vision.common.controller;

import io.github.mavarazo.vision.shared.exception.Alertable;
import io.github.mavarazo.vision.shared.exception.BusinessException;
import io.github.mavarazo.vision.shared.exception.TechnicalException;
import io.micrometer.tracing.BaggageView;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.spi.LoggingEventBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.Optional;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final Tracer tracer;

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(final BusinessException ex) {
        final LoggingEventBuilder loggingEventBuilder = (ex instanceof Alertable) ? log.atError() : log.atWarn();
        loggingEventBuilder.log("Business error: {}, message: {}", ex.getErrorCode(), ex.getMessage().formatted(ex.getParameters()));

        return createProblemDetail(
                ex.getStatus(),
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getParameters()
        );
    }

    @ExceptionHandler(TechnicalException.class)
    public ProblemDetail handleTechnicalException(final TechnicalException ex) {
        if (ex.getMessage() != null) {
            log.error("Technical error: {} | message: {}", ex.getErrorCode(), ex.getMessage().formatted(ex.getParameters()), ex);
        } else {
            log.error("Technical error: {}", ex.getErrorCode(), ex);
        }

        return createProblemDetail(
                ex.getStatus(),
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getParameters()
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(final Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);

        return createProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "vision.system.unhandled_error",
                "A system error occurred.",
                null
        );
    }

    private ProblemDetail createProblemDetail(final HttpStatus status, final String errorCode, final String detail, final Map<String, Object> params) {
        final ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, errorCode);

        problem.setProperty("error_code", errorCode);
        getTraceId().ifPresent(traceId -> problem.setProperty("trace.id", traceId));
        getCorrelationId().ifPresent(correlationId -> problem.setProperty("correlation.id", correlationId));

        if (params != null && !params.isEmpty()) {
            problem.setProperty("parameters", params);
        }

        return problem;
    }

    private Optional<String> getTraceId() {
        return Optional.ofNullable(tracer.currentSpan())
                .map(Span::context)
                .map(TraceContext::traceId);
    }

    private Optional<String> getCorrelationId() {
        return Optional.of(tracer.getBaggage("correlation.id"))
                .map(BaggageView::get);
    }
}
