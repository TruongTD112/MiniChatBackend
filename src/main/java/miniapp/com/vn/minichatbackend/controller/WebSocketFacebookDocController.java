package miniapp.com.vn.minichatbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API mô tả / hướng dẫn riêng cho nền tảng Facebook (channel, WebSocket, …).
 * Sau này có thể thêm ZaloDocController, TelegramDocController, v.v. với path /api/docs/zalo, …
 */
@RestController
@RequestMapping("/api/docs/facebook")
@Tag(name = "Facebook – Docs", description = "Hướng dẫn tích hợp và kết nối cho channel Facebook (Page, WebSocket real-time)")
public class WebSocketFacebookDocController {

    @GetMapping("/websocket-connection")
    @Operation(
            summary = "Facebook – Hướng dẫn kết nối WebSocket (Dashboard real-time)",
            description = """
                    Hướng dẫn kết nối WebSocket để nhận tin nhắn real-time của **channel Facebook** (Page). Các platform khác (Zalo, …) sẽ có endpoint docs riêng.
                    
                    ## 1. Kết nối WebSocket (STOMP + SockJS)
                    
                    - **URL**: `GET {baseUrl}/ws?token={JWT}`
                    - **Ví dụ**: `wss://api.example.com/ws?token=eyJhbGciOiJIUzI1NiIs...`
                    - **Token**: Tham số query `token` bắt buộc, là JWT trả về từ API đăng nhập (BackOffice). Nếu thiếu hoặc sai, handshake vẫn thành công nhưng Principal = null và **subscribe sẽ bị từ chối**.
                    
                    ## 2. Subscribe theo channel (Facebook Page)
                    
                    - **Destination (STOMP)**: `/topic/facebook/channel/{channelId}`
                    - **channelId**: ID của Channel (Facebook Page đã connect). Chỉ user thuộc **business** sở hữu channel đó mới subscribe được; nếu không sẽ bị **403**.
                    - **Ví dụ**: Xem Page có channel `id = 1` → subscribe `/topic/facebook/channel/1`.
                    
                    ## 3. Payload tin nhắn (Facebook)
                    
                    Mỗi tin (user nhắn Page hoặc bot trả lời) server gửi xuống dạng JSON **ConversationMessageWsPayload**; với Facebook thì **platform** = `FACEBOOK`, **senderId**/**recipientId** là PSID hoặc Page ID.
                    
                    - **conversationId** (long): ID cuộc hội thoại – nhóm tin theo conversation.
                    - **channelId** (long): ID channel (Facebook Page).
                    - **messageId** (string): ID tin trong MongoDB.
                    - **externalMessageId** (string): Facebook message id (mid).
                    - **direction** (string): `INBOUND` = user → Page, `OUTBOUND` = Page/bot → user.
                    - **text** (string): Nội dung text.
                    - **senderId** (string): PSID (user) hoặc Page ID (khi bot gửi).
                    - **recipientId** (string): Page ID hoặc PSID.
                    - **platform** (string): `FACEBOOK`.
                    - **createdAt** (string): Thời điểm (ISO-8601).
                    
                    ## 4. Luồng client (Facebook)
                    
                    1. Gọi API đăng nhập → lấy JWT.
                    2. Gọi `POST /api/facebook/channels` với `businessId` → lấy danh sách channel Facebook (có `id`) để user chọn.
                    3. Mở WebSocket: kết nối `{baseUrl}/ws?token={JWT}` (SockJS + STOMP).
                    4. Sau khi CONNECTED, gửi SUBSCRIBE tới `/topic/facebook/channel/{channelId}` (channelId là ID channel Facebook).
                    5. Nhận message: body là JSON; dùng `conversationId` để nhóm theo hội thoại, `direction` để hiển thị user hay bot.
                    
                    ## 5. Ví dụ (JavaScript)
                    
                    ```javascript
                    const token = 'YOUR_JWT';
                    const facebookChannelId = 1;
                    const wsUrl = `${window.location.origin.replace('http', 'ws')}/ws?token=${encodeURIComponent(token)}`;
                    const socket = new SockJS(wsUrl);
                    const client = Stomp.over(socket);
                    client.connect({}, () => {
                      client.subscribe(`/topic/facebook/channel/${facebookChannelId}`, (message) => {
                        const payload = JSON.parse(message.body);
                        console.log(payload.direction, payload.text, payload.conversationId);
                      });
                    });
                    ```
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Thông tin tóm tắt (chi tiết xem description trên)",
                            content = @Content(schema = @Schema(implementation = WebSocketFacebookDocController.FacebookWebSocketDocResponse.class)))
            }
    )
    public ResponseEntity<FacebookWebSocketDocResponse> getFacebookWebSocketConnectionGuide() {
        return ResponseEntity.ok(new FacebookWebSocketDocResponse(
                "GET /ws?token={JWT}",
                "/topic/facebook/channel/{channelId}",
                "FACEBOOK",
                "Chỉ dùng cho channel Facebook. Xem mô tả đầy đủ trong Swagger."
        ));
    }

    @Schema(description = "Thông tin tóm tắt WebSocket – Facebook")
    public record FacebookWebSocketDocResponse(
            @Schema(description = "URL kết nối WebSocket (query token bắt buộc)", example = "GET /ws?token={JWT}")
            String websocketUrl,
            @Schema(description = "Destination STOMP subscribe theo channel Facebook", example = "/topic/facebook/channel/1")
            String subscribeDestination,
            @Schema(description = "Platform", example = "FACEBOOK")
            String platform,
            @Schema(description = "Ghi chú")
            String note
    ) {}
}
