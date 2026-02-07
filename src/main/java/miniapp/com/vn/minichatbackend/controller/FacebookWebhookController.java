package miniapp.com.vn.minichatbackend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniapp.com.vn.minichatbackend.config.FacebookConfig;
import miniapp.com.vn.minichatbackend.dto.webhook.FacebookWebhookPayload;
import miniapp.com.vn.minichatbackend.service.FacebookWebhookService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API nhận webhook từ Facebook Messenger.
 * - GET: Xác thực webhook (Facebook gửi hub.mode, hub.verify_token, hub.challenge).
 * - POST: Nhận sự kiện messaging (tin nhắn), xác định conversation và lưu message vào MongoDB.
 */
@Slf4j
@RestController
@RequestMapping("/api/webhook/facebook")
@RequiredArgsConstructor
public class FacebookWebhookController {

    private final FacebookConfig facebookConfig;
    private final FacebookWebhookService facebookWebhookService;

    /**
     * Xác thực webhook khi đăng ký với Facebook.
     * Facebook gửi: hub.mode=subscribe, hub.verify_token=..., hub.challenge=...
     * Trả về hub.challenge nếu verify_token khớp cấu hình.
     */
    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> verify(
            @RequestParam(name = "hub.mode") String mode,
            @RequestParam(name = "hub.verify_token") String verifyToken,
            @RequestParam(name = "hub.challenge") String challenge) {
        String expectedToken = facebookConfig.getWebhook() != null
                ? facebookConfig.getWebhook().getVerifyToken()
                : "minichat_webhook_verify_token";
        if ("subscribe".equals(mode) && expectedToken.equals(verifyToken)) {
            log.info("Webhook verified successfully");
            return ResponseEntity.ok(challenge);
        }
        log.warn("Webhook verification failed: mode={}, token mismatch", mode);
        return ResponseEntity.status(403).body("Verification failed");
    }

    /**
     * Nhận sự kiện từ Facebook (tin nhắn, delivery, read...).
     * Chỉ xử lý entry có messaging.message; lưu tin nhắn vào MongoDB và gắn với conversation.
     */
    @PostMapping
    public ResponseEntity<String> handleWebhook(@RequestBody FacebookWebhookPayload payload) {
        if (payload == null) {
            return ResponseEntity.ok("ok");
        }
        try {
            facebookWebhookService.handleWebhookPayload(payload);
        } catch (Exception e) {
            log.error("Error processing webhook payload", e);
        }
        return ResponseEntity.ok("ok");
    }
}
