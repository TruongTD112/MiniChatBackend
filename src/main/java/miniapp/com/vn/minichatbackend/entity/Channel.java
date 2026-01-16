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
@Table(name = "Channel")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Channel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String name;

    @Column(length = 50)
    private String platform; // FACEBOOK, ZALO...

    @Column(name = "business_id")
    private Long businessId;

    @Column
    private Integer status;

    @Column
    private String config; // Lưu ý: Mã hóa ở tầng App

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private String settings;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

