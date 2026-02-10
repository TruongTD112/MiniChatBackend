package miniapp.com.vn.minichatbackend.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniapp.com.vn.minichatbackend.common.ErrorCode;
import miniapp.com.vn.minichatbackend.common.Result;
import miniapp.com.vn.minichatbackend.dto.response.ConversationHistoryResponse;
import miniapp.com.vn.minichatbackend.dto.response.FacebookConversationResponse;
import miniapp.com.vn.minichatbackend.dto.response.FacebookUserInfoResponse;
import miniapp.com.vn.minichatbackend.dto.response.SendMessageResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import miniapp.com.vn.minichatbackend.config.FacebookConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service để gọi Facebook Graph API và lấy danh sách Conversations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FacebookConversationService {

    private static final DateTimeFormatter FACEBOOK_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

    private final FacebookConfig facebookConfig;
    private final RestTemplate restTemplate;
    
    /**
     * Lấy danh sách Conversations từ Facebook Page
     * @param pageId Facebook Page ID
     * @param pageAccessToken Page Access Token (đã giải mã)
     * @param limit Số lượng conversations muốn lấy
     * @return Result chứa danh sách conversations hoặc error
     */
    public Result<FacebookConversationResponse> getConversations(String pageId, String pageAccessToken, Integer limit) {
        if (pageId == null || pageId.trim().isEmpty()) {
            log.warn("Page ID is null or empty");
            return Result.error(ErrorCode.INVALID_REQUEST, "Page ID không được để trống");
        }
        
        if (pageAccessToken == null || pageAccessToken.trim().isEmpty()) {
            log.warn("Page access token is null or empty");
            return Result.error(ErrorCode.INVALID_REQUEST, "Page Access Token không được để trống");
        }
        
        int defaultLimit = facebookConfig.getConversation().getDefaultLimit();
        int maxLimit = facebookConfig.getConversation().getMaxLimit();
        int requestLimit = (limit != null && limit > 0) ? Math.min(limit, maxLimit) : defaultLimit;

        try {
            // Gọi Facebook Graph API để lấy conversations
            // Lưu ý: Facebook API có thể không trả về messages trong conversations endpoint
            // Cần gọi riêng endpoint messages nếu muốn lấy chi tiết messages
            String url = facebookConfig.getGraphApi().getUrl() + "/" + pageId + "/conversations" +
                        "?access_token=" + pageAccessToken.trim() +
                        "&fields=" + facebookConfig.getConversation().getFields() +
                        "&limit=" + requestLimit;
            
            log.info("Calling Facebook Graph API to get conversations: pageId={}, limit={}", pageId, requestLimit);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<FacebookConversationsApiResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                FacebookConversationsApiResponse.class
            );
            
            FacebookConversationsApiResponse apiResponse = response.getBody();
            
            if (apiResponse == null || apiResponse.getData() == null) {
                log.warn("Facebook API returned null or empty data");
                return Result.error(ErrorCode.INTERNAL_ERROR, "Không thể lấy dữ liệu từ Facebook API");
            }
            
            // Chuyển đổi từ Facebook API response sang DTO response
            List<FacebookConversationResponse.ConversationInfo> conversations = new ArrayList<>();
            for (FacebookConversationData convData : apiResponse.getData()) {
                // Parse participants
                List<FacebookConversationResponse.ParticipantInfo> participants = new ArrayList<>();
                if (convData.getParticipants() != null && convData.getParticipants().getData() != null) {
                    for (FacebookParticipantData partData : convData.getParticipants().getData()) {
                        participants.add(FacebookConversationResponse.ParticipantInfo.builder()
                            .id(partData.getId())
                            .name(partData.getName())
                            .build());
                    }
                }
                
                // Parse sender
                FacebookConversationResponse.SenderInfo sender = null;
                if (convData.getSender() != null) {
                    sender = FacebookConversationResponse.SenderInfo.builder()
                        .id(convData.getSender().getId())
                        .name(convData.getSender().getName())
                        .build();
                }
                
                // Parse last message (nếu có)
                FacebookConversationResponse.LastMessageInfo lastMessage = null;
                if (convData.getLastMessage() != null) {
                    FacebookMessageData msgData = convData.getLastMessage();
                    FacebookConversationResponse.SenderInfo fromSender = null;
                    if (msgData.getFrom() != null) {
                        fromSender = FacebookConversationResponse.SenderInfo.builder()
                            .id(msgData.getFrom().getId())
                            .name(msgData.getFrom().getName())
                            .build();
                    }
                    
                    LocalDateTime createdTime = null;
                    if (msgData.getCreatedTime() != null) {
                        try {
                            createdTime = LocalDateTime.parse(msgData.getCreatedTime().replace("+0000", "+00:00"), 
                                DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                        } catch (Exception e) {
                            log.warn("Error parsing created_time: {}", msgData.getCreatedTime(), e);
                        }
                    }
                    
                    lastMessage = FacebookConversationResponse.LastMessageInfo.builder()
                        .id(msgData.getId())
                        .message(msgData.getMessage())
                        .createdTime(createdTime)
                        .from(fromSender)
                        .build();
                }
                
                // Parse updated_time
                LocalDateTime updatedTime = null;
                if (convData.getUpdatedTime() != null) {
                    try {
                        updatedTime = LocalDateTime.parse(convData.getUpdatedTime().replace("+0000", "+00:00"), 
                            DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    } catch (Exception e) {
                        log.warn("Error parsing updated_time: {}", convData.getUpdatedTime(), e);
                    }
                }
                
                FacebookConversationResponse.ConversationInfo convInfo = 
                    FacebookConversationResponse.ConversationInfo.builder()
                        .id(convData.getId())
                        .link(convData.getLink())
                        .messageCount(convData.getMessageCount())
                        .participants(participants)
                        .sender(sender)
                        .lastMessage(lastMessage)
                        .updatedTime(updatedTime)
                        .build();
                
                conversations.add(convInfo);
            }
            
            // Parse paging
            FacebookConversationResponse.PagingInfo paging = null;
            if (apiResponse.getPaging() != null) {
                paging = FacebookConversationResponse.PagingInfo.builder()
                    .next(apiResponse.getPaging().getNext())
                    .previous(apiResponse.getPaging().getPrevious())
                    .build();
            }
            
            FacebookConversationResponse responseDto = FacebookConversationResponse.builder()
                .conversations(conversations)
                .paging(paging)
                .build();
            
            log.info("Successfully retrieved {} conversations from Facebook Page: {}", 
                conversations.size(), pageId);
            return Result.success(responseDto);
            
        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("Facebook API returned 401 Unauthorized: {}", e.getMessage());
            return Result.error(ErrorCode.TOKEN_EXPIRED,
                "Token hết hạn hoặc đã bị thu hồi, vui lòng kết nối lại Page.");
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
            log.error("Unexpected error while getting Facebook conversations", e);
            return Result.error(ErrorCode.INTERNAL_ERROR, 
                "Lỗi không mong đợi: " + e.getMessage());
        }
    }

    /**
     * Lấy lịch sử tin nhắn (messages) của một Conversation từ Facebook.
     * Mặc định lấy 25 tin nhắn mới nhất nếu không truyền limit.
     *
     * @param conversationId Conversation ID từ Facebook (vd: t_1234567890)
     * @param pageAccessToken Page Access Token (đã giải mã)
     * @param limit Số tin nhắn muốn lấy, null hoặc <= 0 thì dùng 25
     * @param after Cursor pagination từ Facebook (after), null nếu lấy trang đầu
     * @return Result chứa danh sách messages hoặc error
     */
    public Result<ConversationHistoryResponse> getConversationMessages(
            String conversationId,
            String pageAccessToken,
            Integer limit,
            String after) {
        if (conversationId == null || conversationId.trim().isEmpty()) {
            log.warn("Conversation ID is null or empty");
            return Result.error(ErrorCode.INVALID_REQUEST, "Conversation ID không được để trống");
        }
        if (pageAccessToken == null || pageAccessToken.trim().isEmpty()) {
            log.warn("Page access token is null or empty");
            return Result.error(ErrorCode.INVALID_REQUEST, "Page Access Token không được để trống");
        }

        int defaultLimit = facebookConfig.getConversation().getDefaultLimit();
        int maxLimit = facebookConfig.getConversation().getMaxLimit();
        int requestLimit = (limit != null && limit > 0) ? Math.min(limit, maxLimit) : defaultLimit;

        try {
            String url = facebookConfig.getGraphApi().getUrl() + "/" + conversationId.trim() + "/messages" +
                    "?access_token=" + pageAccessToken.trim() +
                    "&fields=" + facebookConfig.getMessages().getFields() +
                    "&limit=" + requestLimit;
            if (after != null && !after.trim().isEmpty()) {
                url += "&after=" + after.trim();
            }

            log.info("Calling Facebook Graph API to get conversation messages: conversationId={}, limit={}", conversationId, requestLimit);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<FacebookMessagesApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    FacebookMessagesApiResponse.class
            );

            FacebookMessagesApiResponse apiResponse = response.getBody();
            if (apiResponse == null || apiResponse.getData() == null) {
                log.warn("Facebook API returned null or empty data for conversation messages");
                return Result.error(ErrorCode.INTERNAL_ERROR, "Không thể lấy dữ liệu tin nhắn từ Facebook API");
            }

            List<ConversationHistoryResponse.MessageInfo> messages = new ArrayList<>();
            for (FacebookMessageData msg : apiResponse.getData()) {
                ConversationHistoryResponse.SenderInfo fromInfo = null;
                if (msg.getFrom() != null) {
                    fromInfo = ConversationHistoryResponse.SenderInfo.builder()
                            .id(msg.getFrom().getId())
                            .name(msg.getFrom().getName())
                            .build();
                }
                LocalDateTime createdTime = null;
                if (msg.getCreatedTime() != null) {
                    try {
                        createdTime = LocalDateTime.parse(msg.getCreatedTime().replace("+0000", "+00:00"),
                                DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    } catch (Exception e) {
                        log.warn("Error parsing created_time: {}", msg.getCreatedTime(), e);
                    }
                }
                messages.add(ConversationHistoryResponse.MessageInfo.builder()
                        .id(msg.getId())
                        .message(msg.getMessage())
                        .createdTime(createdTime)
                        .from(fromInfo)
                        .build());
            }

            ConversationHistoryResponse.PagingInfo pagingInfo = null;
            if (apiResponse.getPaging() != null) {
                String pagingAfter = null;
                String pagingBefore = null;
                if (apiResponse.getPaging().getCursors() != null) {
                    pagingAfter = apiResponse.getPaging().getCursors().getAfter();
                    pagingBefore = apiResponse.getPaging().getCursors().getBefore();
                }
                pagingInfo = ConversationHistoryResponse.PagingInfo.builder()
                        .after(pagingAfter)
                        .before(pagingBefore)
                        .next(apiResponse.getPaging().getNext())
                        .previous(apiResponse.getPaging().getPrevious())
                        .build();
            }

            ConversationHistoryResponse resultDto = ConversationHistoryResponse.builder()
                    .conversationId(conversationId)
                    .messages(messages)
                    .paging(pagingInfo)
                    .build();

            log.info("Successfully retrieved {} messages for conversationId={}", messages.size(), conversationId);
            return Result.success(resultDto);

        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("Facebook API returned 401 Unauthorized: {}", e.getMessage());
            return Result.error(ErrorCode.TOKEN_EXPIRED,
                    "Token hết hạn hoặc đã bị thu hồi, vui lòng kết nối lại Page.");
        } catch (HttpClientErrorException e) {
            log.error("Facebook API returned error: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            return Result.error(ErrorCode.INTERNAL_ERROR,
                    "Lỗi khi gọi Facebook API: " + e.getStatusCode());
        } catch (RestClientException e) {
            log.error("Error calling Facebook Graph API for messages", e);
            return Result.error(ErrorCode.INTERNAL_ERROR,
                    "Không thể kết nối đến Facebook API: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while getting conversation messages", e);
            return Result.error(ErrorCode.INTERNAL_ERROR,
                    "Lỗi không mong đợi: " + e.getMessage());
        }
    }

    /**
     * Gửi tin nhắn từ Fanpage đến người dùng (chat với fanpage).
     * Dùng Facebook Send API: POST /me/messages với messaging_type RESPONSE.
     *
     * @param pageAccessToken Page Access Token (đã giải mã)
     * @param recipientPsid   Page-Scoped User ID (PSID) người nhận
     * @param text            Nội dung tin nhắn text (optional if imageUrl is present)
     * @param imageUrl        URL ảnh/attachment (optional if text is present)
     * @return Result chứa messageId, recipientId hoặc error
     */
    public Result<SendMessageResponse> sendMessage(String pageAccessToken, String recipientPsid, String text, String imageUrl) {
        if (pageAccessToken == null || pageAccessToken.trim().isEmpty()) {
            log.warn("Page access token is null or empty");
            return Result.error(ErrorCode.INVALID_REQUEST, "Page Access Token không được để trống");
        }
        if (recipientPsid == null || recipientPsid.trim().isEmpty()) {
            log.warn("Recipient PSID is null or empty");
            return Result.error(ErrorCode.INVALID_REQUEST, "recipientId (PSID) không được để trống");
        }
        
        boolean hasText = text != null && !text.trim().isEmpty();
        boolean hasImage = imageUrl != null && !imageUrl.trim().isEmpty();
        
        if (!hasText && !hasImage) {
            log.warn("Both text and imageUrl are null or empty");
            return Result.error(ErrorCode.INVALID_REQUEST, "Nội dung tin nhắn hoặc URL ảnh không được để trống");
        }

        Result<SendMessageResponse> lastResult = null;

        // 1. Send text message if present
        if (hasText) {
            Map<String, Object> message = Collections.singletonMap("text", text.trim());
            lastResult = callFacebookSendApi(pageAccessToken, recipientPsid, message);
            if (!lastResult.isSuccess()) {
                return lastResult;
            }
        }

        // 2. Send image message if present
        if (hasImage) {
            Map<String, Object> imagePayload = new HashMap<>();
            imagePayload.put("url", imageUrl.trim());
            imagePayload.put("is_reusable", true);

            Map<String, Object> attachment = new HashMap<>();
            attachment.put("type", "image");
            attachment.put("payload", imagePayload);

            Map<String, Object> message = Collections.singletonMap("attachment", attachment);
            lastResult = callFacebookSendApi(pageAccessToken, recipientPsid, message);
        }

        return lastResult;
    }

    /**
     * Overload duy trì compatibility cho các chỗ cũ chỉ gửi text.
     */
    public Result<SendMessageResponse> sendMessage(String pageAccessToken, String recipientPsid, String text) {
        return sendMessage(pageAccessToken, recipientPsid, text, null);
    }

    /**
     * Helper thực hiện gọi REST API tới Facebook Send API.
     */
    private Result<SendMessageResponse> callFacebookSendApi(String pageAccessToken, String recipientPsid, Map<String, Object> message) {
        try {
            String url = facebookConfig.getGraphApi().getUrl() + "/me/messages?access_token=" + pageAccessToken.trim();
            Map<String, Object> recipient = Collections.singletonMap("id", recipientPsid.trim());
            
            Map<String, Object> body = new HashMap<>();
            body.put("recipient", recipient);
            body.put("messaging_type", facebookConfig.getSendApi().getMessagingType());
            body.put("message", message);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<FacebookSendMessageApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    FacebookSendMessageApiResponse.class
            );

            FacebookSendMessageApiResponse apiResponse = response.getBody();
            if (apiResponse == null || apiResponse.getMessageId() == null) {
                log.warn("Facebook Send API returned null or missing message_id");
                return Result.error(ErrorCode.INTERNAL_ERROR, "Không nhận được message_id từ Facebook");
            }

            SendMessageResponse resultDto = SendMessageResponse.builder()
                    .messageId(apiResponse.getMessageId())
                    .recipientId(apiResponse.getRecipientId() != null ? apiResponse.getRecipientId() : recipientPsid)
                    .build();
            log.info("Successfully sent message to PSID={}, messageId={}", recipientPsid, apiResponse.getMessageId());
            return Result.success(resultDto);
        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("Facebook API returned 401 Unauthorized: {}", e.getMessage());
            return Result.error(ErrorCode.TOKEN_EXPIRED, "Token hết hạn hoặc đã bị thu hồi, vui lòng kết nối lại Page.");
        } catch (HttpClientErrorException e) {
            log.error("Facebook Send API error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi gửi tin nhắn: " + e.getStatusCode());
        } catch (RestClientException e) {
            log.error("Error calling Facebook Send API", e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Không thể kết nối đến Facebook API: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while calling Facebook API", e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi không mong đợi: " + e.getMessage());
        }
    }

    /**
     * Lấy thông tin người dùng (tên, avatar) từ Facebook theo PSID.
     * Gọi GET /{psid}?fields=name,picture.type(large)
     *
     * @param pageAccessToken Page Access Token (đã giải mã)
     * @param psid            Page-Scoped User ID
     * @return Result chứa id, name, avatarUrl hoặc error
     */
    public Result<FacebookUserInfoResponse> getUserProfile(String pageAccessToken, String psid) {
        if (pageAccessToken == null || pageAccessToken.trim().isEmpty()) {
            log.warn("Page access token is null or empty");
            return Result.error(ErrorCode.INVALID_REQUEST, "Page Access Token không được để trống");
        }
        if (psid == null || psid.trim().isEmpty()) {
            log.warn("PSID is null or empty");
            return Result.error(ErrorCode.INVALID_REQUEST, "psid không được để trống");
        }
        try {
            String url = facebookConfig.getGraphApi().getUrl() + "/" + psid.trim() +
                    "?fields=" + facebookConfig.getUserProfile().getFields() +
                    "&access_token=" + pageAccessToken.trim();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<FacebookUserProfileApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    FacebookUserProfileApiResponse.class
            );

            FacebookUserProfileApiResponse apiResponse = response.getBody();
            if (apiResponse == null) {
                log.warn("Facebook API returned null for user profile");
                return Result.error(ErrorCode.INTERNAL_ERROR, "Không thể lấy thông tin người dùng từ Facebook");
            }

            String avatarUrl = null;
            if (apiResponse.getPicture() != null && apiResponse.getPicture().getData() != null) {
                avatarUrl = apiResponse.getPicture().getData().getUrl();
            }

            FacebookUserInfoResponse resultDto = FacebookUserInfoResponse.builder()
                    .id(psid.trim())
                    .name(apiResponse.getName())
                    .avatarUrl(avatarUrl)
                    .build();
            log.info("Successfully retrieved user profile for PSID={}", psid);
            return Result.success(resultDto);
        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("Facebook API returned 401 Unauthorized: {}", e.getMessage());
            return Result.error(ErrorCode.TOKEN_EXPIRED, "Token hết hạn hoặc đã bị thu hồi, vui lòng kết nối lại Page.");
        } catch (HttpClientErrorException e) {
            log.error("Facebook API error for user profile: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi lấy thông tin người dùng: " + e.getStatusCode());
        } catch (RestClientException e) {
            log.error("Error calling Facebook Graph API for user profile", e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Không thể kết nối đến Facebook API: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while getting user profile", e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi không mong đợi: " + e.getMessage());
        }
    }
    
    /**
     * DTO để map response từ Facebook Graph API - Conversations
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookConversationsApiResponse {
        @JsonProperty("data")
        private List<FacebookConversationData> data;
        
        @JsonProperty("paging")
        private FacebookPagingData paging;
    }
    
    /**
     * DTO để map một Conversation từ Facebook API
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookConversationData {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("link")
        private String link;
        
        @JsonProperty("message_count")
        private Integer messageCount;
        
        @JsonProperty("participants")
        private FacebookParticipantsData participants;
        
        @JsonProperty("sender")
        private FacebookSenderData sender;
        
        @JsonProperty("updated_time")
        private String updatedTime;
        
        @JsonProperty("messages")
        private FacebookMessagesData messages;
        
        // Lấy last message từ messages nếu có
        public FacebookMessageData getLastMessage() {
            if (messages != null && messages.getData() != null && !messages.getData().isEmpty()) {
                return messages.getData().get(0); // Message đầu tiên là message mới nhất
            }
            return null;
        }
    }
    
    /**
     * DTO để map Participants
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookParticipantsData {
        @JsonProperty("data")
        private List<FacebookParticipantData> data;
    }
    
    /**
     * DTO để map Participant
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookParticipantData {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("name")
        private String name;
    }
    
    /**
     * DTO để map Sender
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookSenderData {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("name")
        private String name;
    }
    
    /**
     * DTO để map Messages
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookMessagesData {
        @JsonProperty("data")
        private List<FacebookMessageData> data;
    }
    
    /**
     * DTO để map Message
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookMessageData {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("created_time")
        private String createdTime;
        
        @JsonProperty("from")
        private FacebookSenderData from;
    }
    
    /**
     * DTO để map Paging
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookPagingData {
        @JsonProperty("next")
        private String next;
        
        @JsonProperty("previous")
        private String previous;
    }

    /**
     * DTO để map response từ Facebook Graph API - Messages (GET /{conversation-id}/messages)
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookMessagesApiResponse {
        @JsonProperty("data")
        private List<FacebookMessageData> data;
        
        @JsonProperty("paging")
        private FacebookMessagesPagingData paging;
    }

    /**
     * DTO để map Paging của Messages API (có cursors)
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookMessagesPagingData {
        @JsonProperty("cursors")
        private FacebookPagingCursors cursors;
        
        @JsonProperty("next")
        private String next;
        
        @JsonProperty("previous")
        private String previous;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookPagingCursors {
        @JsonProperty("after")
        private String after;
        
        @JsonProperty("before")
        private String before;
    }

    /**
     * DTO map response từ Facebook Send API (POST /me/messages)
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookSendMessageApiResponse {
        @JsonProperty("recipient_id")
        private String recipientId;
        
        @JsonProperty("message_id")
        private String messageId;
    }

    /**
     * DTO map response từ Facebook GET /{psid}?fields=name,picture
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookUserProfileApiResponse {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("picture")
        private FacebookPictureData picture;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookPictureData {
        @JsonProperty("data")
        private FacebookPictureDataInner data;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookPictureDataInner {
        @JsonProperty("url")
        private String url;
        
        @JsonProperty("height")
        private Integer height;
        
        @JsonProperty("width")
        private Integer width;
    }
}
