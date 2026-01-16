package miniapp.com.vn.minichatbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Product", indexes = {
    @Index(name = "idx_biz_product", columnList = "business_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id")
    private Long businessId;

    @Column(length = 255)
    private String name;

    @Column
    private String description;

    @Column(precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "main_image_url", length = 255)
    private String mainImageUrl;

    @Column(name = "detail_image_url")
    private String detailImageUrl;

    @Column(name = "quantity_avail")
    private Integer quantityAvail;

    /**
     * Trạng thái sản phẩm:
     * - "1" hoặc "available": Còn hàng
     * - "2" hoặc "sold_out": Hết hàng
     * - "3" hoặc "no_longer_sell": Ngừng bán
     */
    @Column(length = 50)
    private String status;

    /**
     * Metadata lưu dạng JSON các thuộc tính khác của sản phẩm:
     * - Kích thước, màu sắc, chất liệu
     * - Thông số kỹ thuật
     * - Tags, categories
     * 
     * Ví dụ:
     * {
     *   "size": ["S", "M", "L", "XL"],
     *   "color": ["Đỏ", "Xanh", "Đen"],
     *   "material": "Cotton 100%",
     *   "specifications": {
     *     "weight": "200g",
     *     "dimensions": "30x40cm"
     *   },
     *   "tags": ["bestseller", "new"],
     *   "category": "Áo thun"
     * }
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private String metadata;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

