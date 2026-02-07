package miniapp.com.vn.minichatbackend.listener;

import miniapp.com.vn.minichatbackend.dto.queue.InboundMessageQueuePayload;

/**
 * Xử lý tin nhắn sau khi lấy từ main queue (đã cập nhật N bản tin gần nhất theo conversation).
 * Có thể inject service gọi AI, gửi thông báo, v.v.
 */
public interface InboundMessageProcessor {

    void process(InboundMessageQueuePayload payload);
}
