package miniapp.com.vn.minichatbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Response DTO sau khi connect Facebook Page thành công
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response sau khi connect Facebook Page thành công")
public class ConnectFacebookPageResponse {
    @Schema(description = "ID của Channel đã tạo", example = "1")
    private Long id;
    
    @Schema(description = "Channel ID (Page ID từ Facebook)", example = "123456789012345")
    private String channelId;
    
    @Schema(description = "Tên của Page", example = "My Business Page")
    private String name;
    
    @Schema(description = "URL avatar của Page", example = "https://scontent.xx.fbcdn.net/v/...")
    private String avatarUrl;
    
    @Schema(description = "Platform (FACEBOOK)", example = "FACEBOOK")
    private String platform;
    
    @Schema(description = "Status của Channel", example = "1")
    private Integer status;
}
