package miniapp.com.vn.minichatbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Business_Intent")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessIntent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id")
    private Long businessId;

    @Column(name = "intent_id")
    private Integer intentId;

    @Column(name = "template_override")
    private String templateOverride;

    @Column
    private Integer status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

