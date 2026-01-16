package miniapp.com.vn.minichatbackend.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductManagementResponse {
    private Long id;
    private Long businessId;
    private String name;
    private String description;
    private BigDecimal price;
    private String mainImageUrl;
    private String detailImageUrl;
    private Integer quantityAvail;
    
    /**
     * Trạng thái sản phẩm: "1"/"available" = Còn hàng, "2"/"sold_out" = Hết hàng, "3"/"no_longer_sell" = Ngừng bán
     */
    private String status;
    
    /**
     * Metadata dạng JSON - Các thuộc tính khác của sản phẩm
     */
    private String metadata;
}

