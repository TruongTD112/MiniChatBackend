package miniapp.com.vn.minichatbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "Conversation", indexes = {
    @Index(name = "idx_last_msg", columnList = "last_message_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "channel_id")
    private Long channelId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "personalization_context")
    private String personalizationContext;

    @Column
    private String summary;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "handler_type", length = 20)
    private String handlerType; // AI, HUMAN

    @Column
    private Integer status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

