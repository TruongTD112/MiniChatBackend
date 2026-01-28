package miniapp.com.vn.minichatbackend.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniapp.com.vn.minichatbackend.common.ErrorCode;
import miniapp.com.vn.minichatbackend.common.Result;
import miniapp.com.vn.minichatbackend.dto.response.FacebookPageResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Service để gọi Facebook Graph API và lấy danh sách Pages
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FacebookPageService {
    
    private static final String FACEBOOK_GRAPH_API_BASE_URL = "https://graph.facebook.com/v24.0";
    private static final String GET_PAGES_ENDPOINT = "/me/accounts";
    
    private final RestTemplate restTemplate;
    
    /**
     * Lấy danh sách Facebook Pages từ user token (bao gồm page access token)
     * @param userToken Facebook user access token
     * @return Result chứa danh sách pages với access token hoặc error
     */
    public Result<FacebookPageResponse> getPages(String userToken) {
        if (userToken == null || userToken.trim().isEmpty()) {
            log.warn("Facebook user token is null or empty");
            return Result.error(ErrorCode.INVALID_REQUEST, "Facebook user token không được để trống");
        }
        
        try {
            // Gọi Facebook Graph API để lấy danh sách pages (bao gồm access_token)
            String url = FACEBOOK_GRAPH_API_BASE_URL + GET_PAGES_ENDPOINT + 
                        "?access_token=" + userToken.trim() + 
                        "&fields=id,name,picture,access_token";
            
            log.info("Calling Facebook Graph API: {}", url.replace(userToken, "***"));
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<FacebookGraphApiResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                FacebookGraphApiResponse.class
            );
            
            FacebookGraphApiResponse apiResponse = response.getBody();
            
            if (apiResponse == null || apiResponse.getData() == null) {
                log.warn("Facebook API returned null or empty data");
                return Result.error(ErrorCode.INTERNAL_ERROR, "Không thể lấy dữ liệu từ Facebook API");
            }
            
            // Chuyển đổi từ Facebook API response sang DTO response
            List<FacebookPageResponse.FacebookPageInfo> pages = new ArrayList<>();
            for (FacebookPageData pageData : apiResponse.getData()) {
                String avatarUrl = null;
                if (pageData.getPicture() != null && 
                    pageData.getPicture().getData() != null) {
                    avatarUrl = pageData.getPicture().getData().getUrl();
                }
                
                FacebookPageResponse.FacebookPageInfo pageInfo = 
                    FacebookPageResponse.FacebookPageInfo.builder()
                        .pageId(pageData.getId())
                        .name(pageData.getName())
                        .avatarUrl(avatarUrl)
                        .build();
                
                pages.add(pageInfo);
            }
            
            FacebookPageResponse responseDto = FacebookPageResponse.builder()
                .pages(pages)
                .build();
            
            log.info("Successfully retrieved {} pages from Facebook", pages.size());
            return Result.success(responseDto);
            
        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("Facebook API returned 401 Unauthorized: {}", e.getMessage());
            return Result.error(ErrorCode.UNAUTHORIZED, "Facebook token không hợp lệ hoặc đã hết hạn");
        } catch (HttpClientErrorException e) {
            log.error("Facebook API returned error: status={}, body={}", 
                e.getStatusCode(), e.getResponseBodyAsString(), e);
            return Result.error(ErrorCode.INTERNAL_ERROR, 
                "Lỗi khi gọi Facebook API: " + e.getStatusCode());
        } catch (RestClientException e) {
            log.error("Error calling Facebook Graph API", e);
            return Result.error(ErrorCode.INTERNAL_ERROR, 
                "Không thể kết nối đến Facebook API: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while getting Facebook pages", e);
            return Result.error(ErrorCode.INTERNAL_ERROR, 
                "Lỗi không mong đợi: " + e.getMessage());
        }
    }
    
    /**
     * DTO để map response từ Facebook Graph API
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookGraphApiResponse {
        @JsonProperty("data")
        private List<FacebookPageData> data;
        
        @JsonProperty("paging")
        private Object paging; // Có thể có paging nếu có nhiều pages
    }
    
    /**
     * DTO để map thông tin một Page từ Facebook API
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookPageData {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("picture")
        private FacebookPicture picture;
        
        @JsonProperty("access_token")
        private String accessToken;
    }
    
    /**
     * DTO để map picture object từ Facebook API
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookPicture {
        @JsonProperty("data")
        private FacebookPictureData data;
    }
    
    /**
     * DTO để map picture data từ Facebook API
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookPictureData {
        @JsonProperty("url")
        private String url;
        
        @JsonProperty("is_silhouette")
        private Boolean isSilhouette;
    }
    
    /**
     * Lấy thông tin một Page cụ thể bao gồm access token từ user token
     * @param userToken Facebook user access token
     * @param pageId ID của page cần lấy
     * @return FacebookPageData với access token hoặc null nếu không tìm thấy
     */
    public FacebookPageData getPageWithAccessToken(String userToken, String pageId) {
        if (userToken == null || userToken.trim().isEmpty() || pageId == null || pageId.trim().isEmpty()) {
            log.warn("Facebook user token or pageId is null or empty");
            return null;
        }
        
        try {
            // Gọi Facebook Graph API để lấy danh sách pages (bao gồm access_token)
            String url = FACEBOOK_GRAPH_API_BASE_URL + GET_PAGES_ENDPOINT + 
                        "?access_token=" + userToken.trim() + 
                        "&fields=id,name,picture,access_token";
            
            log.info("Calling Facebook Graph API to get page: pageId={}", pageId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<FacebookGraphApiResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                FacebookGraphApiResponse.class
            );
            
            FacebookGraphApiResponse apiResponse = response.getBody();
            
            if (apiResponse == null || apiResponse.getData() == null) {
                log.warn("Facebook API returned null or empty data");
                return null;
            }
            
            // Tìm page có id trùng với pageId
            for (FacebookPageData pageData : apiResponse.getData()) {
                if (pageId.equals(pageData.getId())) {
                    log.info("Found page: pageId={}, name={}", pageData.getId(), pageData.getName());
                    return pageData;
                }
            }
            
            log.warn("Page not found: pageId={}", pageId);
            return null;
            
        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("Facebook API returned 401 Unauthorized: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Error getting page with access token", e);
            return null;
        }
    }
}
