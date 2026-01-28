package miniapp.com.vn.minichatbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * Response DTO chứa danh sách Facebook Pages
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response chứa danh sách Facebook Pages")
public class FacebookPageResponse {
    @Schema(description = "Danh sách các Facebook Pages mà user có quyền quản lý")
    private List<FacebookPageInfo> pages;
    
    /**
     * Thông tin của một Facebook Page
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "Thông tin của một Facebook Page")
    public static class FacebookPageInfo {
        /**
         * Page ID của Facebook
         */
        @Schema(description = "Page ID của Facebook", example = "123456789012345")
        private String pageId;
        
        /**
         * Tên của Page
         */
        @Schema(description = "Tên của Facebook Page", example = "My Business Page")
        private String name;
        
        /**
         * URL avatar của Page
         */
        @Schema(description = "URL avatar của Facebook Page", example = "https://scontent.xx.fbcdn.net/v/...")
        private String avatarUrl;
    }
}
