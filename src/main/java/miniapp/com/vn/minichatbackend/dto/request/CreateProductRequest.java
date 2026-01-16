package miniapp.com.vn.minichatbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateProductRequest {
    @NotNull(message = "Business ID không được để trống")
    private Long businessId;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm không được vượt quá 255 ký tự")
    private String name;

    private String description;

    private BigDecimal price;

    @Size(max = 255, message = "URL ảnh chính không được vượt quá 255 ký tự")
    private String mainImageUrl;

    private String detailImageUrl;

    private Integer quantityAvail;

    @Schema(
        description = "Trạng thái sản phẩm:\n" +
                     "- \"1\" hoặc \"available\": Còn hàng\n" +
                     "- \"2\" hoặc \"sold_out\": Hết hàng\n" +
                     "- \"3\" hoặc \"no_longer_sell\": Ngừng bán",
        example = "1",
        allowableValues = {"1", "2", "3", "available", "sold_out", "no_longer_sell"}
    )
    private String status;

    @Schema(
        description = "Metadata dạng JSON - Lưu các thuộc tính khác của sản phẩm:\n" +
                     "- Kích thước, màu sắc, chất liệu\n" +
                     "- Thông số kỹ thuật\n" +
                     "- Tags, categories\n\n" +
                     "Ví dụ:\n" +
                     "{\n" +
                     "  \"size\": [\"S\", \"M\", \"L\", \"XL\"],\n" +
                     "  \"color\": [\"Đỏ\", \"Xanh dương\", \"Đen\"],\n" +
                     "  \"material\": \"Cotton 100%\",\n" +
                     "  \"specifications\": {\n" +
                     "    \"weight\": \"200g\",\n" +
                     "    \"dimensions\": \"30x40cm\"\n" +
                     "  },\n" +
                     "  \"tags\": [\"bestseller\", \"new\"],\n" +
                     "  \"category\": \"Áo thun\"\n" +
                     "}",
        example = "{\"size\": [\"S\", \"M\", \"L\"], \"color\": [\"Đỏ\", \"Xanh\"], \"material\": \"Cotton 100%\"}"
    )
    private String metadata;
}

