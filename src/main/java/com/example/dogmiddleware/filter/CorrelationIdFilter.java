package com.example.dogmiddleware.filter;

import com.example.dogmiddleware.constant.HeaderConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final Pattern VALID_CORRELATION_ID = Pattern.compile("[A-Za-z0-9._-]{1,128}");
    private static final String MDC_CORRELATION_ID = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestedId = request.getHeader(HeaderConstants.CORRELATION_ID);
        String correlationId = requestedId != null && VALID_CORRELATION_ID.matcher(requestedId).matches()
                ? requestedId : UUID.randomUUID().toString();

        MDC.put(MDC_CORRELATION_ID, correlationId);
        response.setHeader(HeaderConstants.CORRELATION_ID, correlationId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_CORRELATION_ID);
        }
    }
}
