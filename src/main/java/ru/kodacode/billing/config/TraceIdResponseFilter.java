package ru.kodacode.billing.config;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Возвращает trace id в ответе, чтобы поддержка могла найти запрос клиента в логах.
 * Gateway передаёт контекст трассировки в заголовках B3.
 */
@Component
public class TraceIdResponseFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    private final Tracer tracer;

    public TraceIdResponseFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        Span span = tracer.currentSpan();
        if (span != null) {
            response.setHeader(TRACE_ID_HEADER, span.context().traceId());
        }
        chain.doFilter(request, response);
    }
}
