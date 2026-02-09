package miniapp.com.vn.minichatbackend.dto.queue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Một lượt hội thoại (user hoặc assistant) lưu trong Redis theo conversation.
 * Dùng để format request gửi AI Core và lưu thêm reply từ AI.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationTurn implements Serializable {

    private static final long serialVersionUID = 1L;

    /** user | assistant */
    private String role;
    private String content;
}
