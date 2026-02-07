package miniapp.com.vn.minichatbackend.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Payload POST từ Facebook Webhook (object=page).
 * Ref: https://developers.facebook.com/docs/messenger-platform/webhooks
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FacebookWebhookPayload {

    private String object;

    @JsonProperty("entry")
    private List<FacebookWebhookEntry> entries;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookWebhookEntry {
        @JsonProperty("id")
        private String pageId;
        @JsonProperty("time")
        private Long time;
        @JsonProperty("messaging")
        private List<FacebookWebhookMessaging> messaging;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookWebhookMessaging {
        @JsonProperty("sender")
        private FacebookWebhookParticipant sender;
        @JsonProperty("recipient")
        private FacebookWebhookParticipant recipient;
        @JsonProperty("timestamp")
        private Long timestamp;
        @JsonProperty("message")
        private FacebookWebhookMessage message;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookWebhookParticipant {
        @JsonProperty("id")
        private String id;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookWebhookMessage {
        @JsonProperty("mid")
        private String mid;
        @JsonProperty("text")
        private String text;
        @JsonProperty("attachments")
        private List<FacebookWebhookAttachment> attachments;
        /** true = tin do Page gửi (echo), false/không có = tin từ người dùng (chỉ lưu loại này) */
        @JsonProperty("is_echo")
        private Boolean isEcho;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookWebhookAttachment {
        @JsonProperty("type")
        private String type;
        @JsonProperty("payload")
        private FacebookWebhookAttachmentPayload payload;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookWebhookAttachmentPayload {
        @JsonProperty("url")
        private String url;
    }
}
