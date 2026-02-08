package miniapp.com.vn.minichatbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniapp.com.vn.minichatbackend.config.AiCoreProperties;
import miniapp.com.vn.minichatbackend.dto.request.AiCoreChatRequest;
import miniapp.com.vn.minichatbackend.dto.response.AiCoreChatResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Gọi AI Core để xử lý tin nhắn chat.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiCoreChatService {

    private final RestTemplate restTemplate;
    private final AiCoreProperties aiCoreProperties;

    /**
     * Gọi POST /api/chat/message tới AI Core.
     *
     * @param request body (message, conversations, customer_id, business_id)
     * @return response hoặc null nếu lỗi
     */
    public AiCoreChatResponse sendMessage(AiCoreChatRequest request) {
        String url = aiCoreProperties.getChatMessageUrl();
        if (url == null || url.isEmpty()) {
            log.warn("AI Core base URL chưa cấu hình (app.ai-core.base-url)");
            return null;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<AiCoreChatRequest> entity = new HttpEntity<>(request, headers);
            AiCoreChatResponse response = restTemplate.postForObject(url, entity, AiCoreChatResponse.class);
            if (response != null && "200".equals(response.getCode()) && response.getData() != null) {
                log.debug("AI Core response: code={} responseLength={}", response.getCode(),
                        response.getData().getResponse() != null ? response.getData().getResponse().length() : 0);
                return response;
            }
            if (response != null) {
                log.warn("AI Core returned non-success: code={} message={}", response.getCode(), response.getMessage());
            }
            return response;
        } catch (RestClientException e) {
            log.error("AI Core call failed: url={} error={}", url, e.getMessage(), e);
            return null;
        }
    }
}
