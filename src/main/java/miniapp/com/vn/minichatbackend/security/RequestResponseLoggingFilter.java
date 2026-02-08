package miniapp.com.vn.minichatbackend.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import miniapp.com.vn.minichatbackend.common.LoggingUtils;
import miniapp.com.vn.minichatbackend.config.LoggingConfig;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Slf4j
@Component
@Order(1) // Highest priority - should run first
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_KEY = "requestId";
    private static final int CACHE_LIMIT = 10 * 1024 * 1024;

    @Autowired
    private LoggingConfig loggingConfig;

    /**
     * Generate a 16-digit request ID
     */
    private String generateRequestId() {
        Random random = new Random();
        return String.format("%016d", Math.abs(random.nextLong()) % 10000000000000000L);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Generate request ID for all requests (even if logging is disabled)
        String requestId = generateRequestId();
        MDC.put(REQUEST_ID_KEY, requestId);
        response.addHeader("X-Request-Id", requestId);

        // Check if logging is enabled
        if (loggingConfig != null && !loggingConfig.isEnabled()) {
            try {
                filterChain.doFilter(request, response);
            } finally {
                // Clear MDC even if logging is disabled
                MDC.clear();
            }
            return;
        }

        // Wrap request (reads & caches body now so we can log before doFilter) and response
        CachedBodyRequestWrapper wrappedRequest = new CachedBodyRequestWrapper(request, CACHE_LIMIT);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            // Log full request (headers + body) before controller runs
            logRequest(wrappedRequest, requestId);

            filterChain.doFilter(wrappedRequest, wrappedResponse);

        } finally {
            logResponse(wrappedResponse, requestId, startTime);

            // Copy response body back to original response
            wrappedResponse.copyBodyToResponse();

            // Clear MDC to prevent memory leaks
            MDC.clear();
        }
    }

    /** Log full request (headers + body) before controller runs. */
    private void logRequest(CachedBodyRequestWrapper request, String requestId) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullUrl = queryString != null ? uri + "?" + queryString : uri;

        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }

        log.info("=== REQUEST ===\nMethod: {}\nURL: {}\nHeaders: {}\nRequestId: {}",
                method, fullUrl, LoggingUtils.formatHeaders(headers), requestId);

        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            String contentType = request.getContentType();
            String body = new String(content, StandardCharsets.UTF_8);
            String maskedBody = LoggingUtils.maskSensitiveData(body);
            String truncatedBody = LoggingUtils.truncateBody(maskedBody);
            log.info("Request Body: \nContent-Type: {}\n{}", contentType, truncatedBody);
        }
    }

    private void logResponse(ContentCachingResponseWrapper response, String requestId, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        int status = response.getStatus();

        // Collect and format response headers
        Map<String, String> headers = new HashMap<>();
        for (String headerName : response.getHeaderNames()) {
            String headerValue = response.getHeader(headerName);
            headers.put(headerName, headerValue);
        }

        log.info("=== RESPONSE ===\nStatus: {}\nDuration: {}ms\nHeaders: {}\n",
                status, duration, LoggingUtils.formatHeaders(headers));

        // Log response body if present and loggable
        String contentType = response.getContentType();
//        if (LoggingUtils.isLoggableContentType(contentType)) {
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            String body = new String(content, StandardCharsets.UTF_8);
            String maskedBody = LoggingUtils.maskSensitiveData(body);
            String truncatedBody = LoggingUtils.truncateBody(maskedBody);
            log.info("Response Body: \nContent-Type: {}\n{}",
                    contentType, truncatedBody);
        }
    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip logging for health check and static resources
        String path = request.getRequestURI();
        return path.startsWith("/health-check") ||
                path.startsWith("/static/") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/");
    }
}
