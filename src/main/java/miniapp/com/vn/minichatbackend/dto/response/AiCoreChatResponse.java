package miniapp.com.vn.minichatbackend.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response từ AI Core POST /api/chat/message.
 * {"code":"200","message":"...","data":{"response":"...","intent":null,"confidence":null}}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiCoreChatResponse {

    private String code;
    private String message;
    private DataPayload data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataPayload {
        private String response;
        private String intent;
        private Double confidence;
    }
}
