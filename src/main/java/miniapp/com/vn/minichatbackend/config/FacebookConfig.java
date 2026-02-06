package miniapp.com.vn.minichatbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình Facebook Graph API, binding từ application.properties (prefix: facebook).
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "facebook")
public class FacebookConfig {

    /** App ID từ Facebook Developer Console (bắt buộc để đổi token sang long-lived) */
    private String appId = "";
    /** App Secret từ Facebook Developer Console (bắt buộc để đổi token sang long-lived) */
    private String appSecret = "";
    private GraphApi graphApi = new GraphApi();
    /** Đường dẫn đổi short-lived token sang long-lived (60 ngày) */
    private String oauthTokenExchangePath = "/oauth/access_token";
    private String pagesEndpoint = "/me/accounts";
    private String pagesFields = "id,name,picture,access_token";
    private Conversation conversation = new Conversation();
    private Messages messages = new Messages();
    private SendApi sendApi = new SendApi();
    private UserProfile userProfile = new UserProfile();
    private String platformName = "FACEBOOK";

    @Data
    public static class GraphApi {
        private String url = "https://graph.facebook.com/v24.0";
    }

    @Data
    public static class Conversation {
        private int defaultLimit = 25;
        private int maxLimit = 100;
        private String fields = "id,link,message_count,participants,sender,updated_time";
    }

    @Data
    public static class Messages {
        private String fields = "id,message,created_time,from";
    }

    @Data
    public static class SendApi {
        private String messagingType = "RESPONSE";
    }

    @Data
    public static class UserProfile {
        private String fields = "name,picture.type(large)";
    }
}
