package miniapp.com.vn.minichatbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "logging.request-response")
@Data
public class LoggingConfig {
    
    /**
     * Enable/disable request-response logging
     */
    private boolean enabled = true;
    
    /**
     * Maximum body length to log (in characters)
     */
    private int maxBodyLength = 10000;
    
    /**
     * Whether to mask sensitive data in logs
     */
    private boolean maskSensitiveData = true;
    
    /**
     * Whether to log request/response headers
     */
    private boolean logHeaders = true;
    
    /**
     * Whether to log request/response bodies
     */
    private boolean logBodies = true;
    
    /**
     * Content types that should be logged
     */
    private String[] loggableContentTypes = {
        "application/json",
        "application/xml", 
        "text/plain",
        "text/html",
        "application/x-www-form-urlencoded"
    };
}
