package miniapp.com.vn.minichatbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình queue xử lý tin nhắn webhook: delayed queue -> main queue, và N tin gần nhất theo conversation.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.webhook.queue")
public class WebhookQueueProperties {

    private boolean producerEnabled = true;
    private boolean listenerEnabled = true;
    /** Thời gian (giây) tin nằm trong delayed queue trước khi chuyển sang main queue */
    private long delaySeconds = 5L;
    /** Tên key Redis của main queue (list) */
    private String mainQueueName = "minichat:message:main";
    /** Số bản tin tối đa giữ theo từng conversation (user + bot) trong Redis */
    private int conversationMaxMessages = 50;
    /** Prefix key Redis: {prefix}{conversationId} = list N tin gần nhất */
    private String conversationRecentPrefix = "minichat:conv:recent:";
}
