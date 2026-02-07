package miniapp.com.vn.minichatbackend.listener;

import lombok.extern.slf4j.Slf4j;
import miniapp.com.vn.minichatbackend.dto.queue.InboundMessageQueuePayload;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * Processor mặc định: chỉ log. Thay bằng bean khác (AI, bot reply...) khi cần.
 */
@Slf4j
@Component
@ConditionalOnMissingBean(InboundMessageProcessor.class)
public class DefaultInboundMessageProcessor implements InboundMessageProcessor {

    @Override
    public void process(InboundMessageQueuePayload payload) {
        log.info("Process inbound message: conversationId={} messageId={} text={}",
                payload.getConversationId(), payload.getMessageId(),
                payload.getText() != null && payload.getText().length() > 50
                        ? payload.getText().substring(0, 50) + "..."
                        : payload.getText());
    }
}
