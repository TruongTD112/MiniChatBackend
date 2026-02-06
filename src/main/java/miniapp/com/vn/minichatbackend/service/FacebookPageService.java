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
import miniapp.com.vn.minichatbackend.config.FacebookConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Service để gọi Facebook Graph API và lấy danh sách Pages.
 * Khi có app-id và app-secret, user token sẽ được đổi sang long-lived (60 ngày) trước khi gọi API,
 * page access token lưu DB sẽ không bị hết hạn sớm.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FacebookPageService {

    private final FacebookConfig facebookConfig;
    private final RestTemplate restTemplate;

    /**
     * Đổi short-lived user token sang long-lived (khoảng 60 ngày).
     * Nếu chưa cấu hình app-id/app-secret thì trả về token gốc.
     */
    public String exchangeUserTokenForLongLived(String shortLivedUserToken) {
        if (shortLivedUserToken == null || shortLivedUserToken.isBlank()) {
            return shortLivedUserToken;
        }
        String appId = facebookConfig.getAppId();
        String appSecret = facebookConfig.getAppSecret();
        if (appId == null || appId.isBlank() || appSecret == null || appSecret.isBlank()) {
            log.debug("Facebook app-id/app-secret chưa cấu hình, dùng token gốc (có thể hết hạn sớm)");
            return shortLivedUserToken.trim();
        }
        try {
            String url = facebookConfig.getGraphApi().getUrl() + facebookConfig.getOauthTokenExchangePath() +
                    "?grant_type=fb_exchange_token" +
                    "&client_id=" + appId +
                    "&client_secret=" + appSecret +
                    "&fb_exchange_token=" + shortLivedUserToken.trim();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            ResponseEntity<FacebookTokenExchangeResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    FacebookTokenExchangeResponse.class
            );
            FacebookTokenExchangeResponse body = response.getBody();
            if (body != null && body.getAccessToken() != null && !body.getAccessToken().isBlank()) {
                log.info("Đã đổi user token sang long-lived (expires_in={}s)", body.getExpiresIn());
                return body.getAccessToken();
            }
        } catch (HttpClientErrorException e) {
            log.warn("Đổi token long-lived thất bại: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.warn("Lỗi khi đổi token long-lived: {}", e.getMessage());
        }
        return shortLivedUserToken.trim();
    }

    /**
     * Lấy danh sách Facebook Pages từ user token (bao gồm page access token).
     * User token sẽ được đổi sang long-lived trước khi gọi /me/accounts.
     */
    public Result<FacebookPageResponse> getPages(String userToken) {
        if (userToken == null || userToken.trim().isEmpty()) {
            log.warn("Facebook user token is null or empty");
            return Result.error(ErrorCode.INVALID_REQUEST, "Facebook user token không được để trống");
        }
        String tokenToUse = exchangeUserTokenForLongLived(userToken);
        try {
            String url = facebookConfig.getGraphApi().getUrl() + facebookConfig.getPagesEndpoint() +
                        "?access_token=" + tokenToUse +
                        "&fields=" + facebookConfig.getPagesFields();

            log.info("Calling Facebook Graph API: {}", url.replace(tokenToUse, "***"));
            
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
            return Result.error(ErrorCode.TOKEN_EXPIRED, "Token hết hạn hoặc đã bị thu hồi, vui lòng kết nối lại Page.");
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
    
    /** DTO response từ GET oauth/access_token (grant_type=fb_exchange_token) */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookTokenExchangeResponse {
        @JsonProperty("access_token")
        private String accessToken;
        @JsonProperty("token_type")
        private String tokenType;
        @JsonProperty("expires_in")
        private Long expiresIn;
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
     * Lấy thông tin một Page cụ thể bao gồm access token từ user token.
     * User token được đổi sang long-lived trước; page token trả về từ /me/accounts (dùng long-lived user token)
     * sẽ là long-lived/không hết hạn, phù hợp lưu DB.
     */
    public FacebookPageData getPageWithAccessToken(String userToken, String pageId) {
        if (userToken == null || userToken.trim().isEmpty() || pageId == null || pageId.trim().isEmpty()) {
            log.warn("Facebook user token or pageId is null or empty");
            return null;
        }
        String tokenToUse = exchangeUserTokenForLongLived(userToken);
        try {
            String url = facebookConfig.getGraphApi().getUrl() + facebookConfig.getPagesEndpoint() +
                        "?access_token=" + tokenToUse +
                        "&fields=" + facebookConfig.getPagesFields();

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
