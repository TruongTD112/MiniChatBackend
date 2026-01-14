package miniapp.com.vn.minichatbackend.common;

import lombok.extern.slf4j.Slf4j;
import miniapp.com.vn.minichatbackend.config.LoggingConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class LoggingUtils {
    
    private static LoggingConfig config;
    
    @Autowired
    public void setLoggingConfig(LoggingConfig loggingConfig) {
        LoggingUtils.config = loggingConfig;
    }
    
    private static final List<String> SENSITIVE_HEADERS = Arrays.asList(
        "authorization", "cookie", "x-api-key", "x-auth-token"
    );
    private static final List<String> SENSITIVE_FIELDS = Arrays.asList(
        "password", "token", "secret", "key", "credential"
    );
    
    /**
     * Truncate body content if it's too long
     */
    public static String truncateBody(String body) {
        if (body == null || config == null || body.length() <= config.getMaxBodyLength()) {
            return body;
        }
        return body.substring(0, config.getMaxBodyLength()) + 
               "... [TRUNCATED - " + body.length() + " characters]";
    }
    
    /**
     * Mask sensitive header values
     */
    public static String maskHeaderValue(String headerName, String headerValue) {
        if (headerValue == null || config == null || !config.isMaskSensitiveData()) {
            return headerValue;
        }
        
        if (SENSITIVE_HEADERS.contains(headerName.toLowerCase())) {
            if (headerValue.length() <= 10) {
                return "***";
            }
            return headerValue.substring(0, 4) + "***" + headerValue.substring(headerValue.length() - 4);
        }
        return headerValue;
    }
    
    /**
     * Mask sensitive fields in JSON body
     */
    public static String maskSensitiveData(String body) {
        if (body == null || config == null || !config.isMaskSensitiveData()) {
            return body;
        }
        
        String maskedBody = body;
        for (String field : SENSITIVE_FIELDS) {
            // Simple regex to mask password-like fields in JSON
            String pattern = "\"" + field + "\"\\s*:\\s*\"([^\"]*)\"";
            maskedBody = maskedBody.replaceAll(pattern, "\"" + field + "\": \"***\"");
        }
        return maskedBody;
    }
    
    /**
     * Format headers for logging
     */
    public static String formatHeaders(java.util.Map<String, String> headers) {
        if (headers == null || headers.isEmpty() || (config != null && !config.isLogHeaders())) {
            return "{}";
        }
        
        StringBuilder sb = new StringBuilder();
        headers.forEach((name, value) -> {
            String maskedValue = maskHeaderValue(name, value);
            sb.append("\n  ").append(name).append(": ").append(maskedValue);
        });
        return sb.toString();
    }
    
    /**
     * Check if content type is loggable
     */
    public static boolean isLoggableContentType(String contentType) {
        if (contentType == null || config == null || !config.isLogBodies()) {
            return false;
        }
        
        String lowerContentType = contentType.toLowerCase();
        for (String loggableType : config.getLoggableContentTypes()) {
            if (lowerContentType.contains(loggableType.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
