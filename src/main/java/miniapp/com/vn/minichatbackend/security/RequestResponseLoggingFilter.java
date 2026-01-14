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
import org.springframework.web.util.ContentCachingRequestWrapper;
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

        // Check if logging is enabled
        if (loggingConfig != null && !loggingConfig.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Generate request ID for all requests
        String requestId = generateRequestId();
        MDC.put(REQUEST_ID_KEY, requestId);
        response.addHeader("X-Request-Id", requestId);

        // Wrap request and response to enable multiple reads
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, CACHE_LIMIT);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            // Log request details
            logRequest(wrappedRequest, requestId);

            // Continue with the filter chain
            filterChain.doFilter(wrappedRequest, wrappedResponse);

        } finally {
            // Log response details
            logResponse(wrappedResponse, requestId, startTime);

            // Copy response body back to original response
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request, String requestId) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullUrl = queryString != null ? uri + "?" + queryString : uri;

        // Collect and format request headers
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.put(headerName, headerValue);
        }

        log.info("=== REQUEST ===\nMethod: {}\nURL: {}\nHeaders: {}\nRequestId: {}",
                method, fullUrl, LoggingUtils.formatHeaders(headers), requestId);

        // Log request body if present and loggable
        String contentType = request.getContentType();
//        if (LoggingUtils.isLoggableContentType(contentType)) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
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
