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
@Table(name = "Business")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Business {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String address;

    @Column
    private String description;

    @Column
    private Integer status; // 1: active, 0: inactive

    /**
     * Metadata lưu dạng JSON các thông tin khác của business:
     * - Chính sách (policies): chính sách đổi trả, bảo hành, vận chuyển
     * - Chương trình khuyến mãi (promotions): các chương trình đang diễn ra
     * 
     * Ví dụ:
     * {
     *   "policies": {
     *     "returnPolicy": "Đổi trả trong 7 ngày",
     *     "warranty": "Bảo hành 12 tháng",
     *     "shipping": "Miễn phí vận chuyển đơn hàng trên 500k"
     *   },
     *   "promotions": [
     *     {
     *       "name": "Giảm 20% tháng 1",
     *       "startDate": "2024-01-01",
     *       "endDate": "2024-01-31"
     *     }
     *   ]
     * }
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private String metadata;

    /**
     * Style lưu dạng JSON style mong muốn của cửa hàng:
     * - Tone: giọng điệu giao tiếp (thân thiện, chuyên nghiệp, vui vẻ...)
     * - Color: màu sắc chủ đạo
     * - Font: font chữ
     * - Language: ngôn ngữ
     * 
     * Ví dụ:
     * {
     *   "tone": "thân thiện",
     *   "color": {
     *     "primary": "#FF6B6B",
     *     "secondary": "#4ECDC4"
     *   },
     *   "font": "Arial",
     *   "language": "vi"
     * }
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private String style;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

