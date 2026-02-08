package miniapp.com.vn.minichatbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình kết nối tới AI Core (xử lý tin nhắn chat).
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.ai-core")
public class AiCoreProperties {

    /** Base URL của AI Core service (vd: http://localhost:8000) */
    private String baseUrl = "http://localhost:8000";
    /** Path gọi chat message (vd: /api/chat/message) */
    private String chatMessagePath = "/api/chat/message";

    public String getChatMessageUrl() {
        String base = baseUrl != null ? baseUrl.trim() : "";
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String path = chatMessagePath != null ? chatMessagePath.trim() : "";
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return base + path;
    }
}
