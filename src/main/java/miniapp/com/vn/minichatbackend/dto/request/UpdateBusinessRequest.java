package miniapp.com.vn.minichatbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBusinessRequest {
    @Size(max = 255, message = "Tên business không được vượt quá 255 ký tự")
    String name;

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    String phone;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    String address;

    String description;
    Integer status; // 1: active, 0: inactive

    @Schema(
        description = "Metadata dạng JSON - Lưu các thông tin khác của business:\n" +
                     "- Chính sách (policies): chính sách đổi trả, bảo hành, vận chuyển\n" +
                     "- Chương trình khuyến mãi (promotions): các chương trình đang diễn ra\n\n" +
                     "Ví dụ:\n" +
                     "{\n" +
                     "  \"policies\": {\n" +
                     "    \"returnPolicy\": \"Đổi trả trong 7 ngày\",\n" +
                     "    \"warranty\": \"Bảo hành 12 tháng\",\n" +
                     "    \"shipping\": \"Miễn phí vận chuyển đơn hàng trên 500k\"\n" +
                     "  },\n" +
                     "  \"promotions\": [\n" +
                     "    {\n" +
                     "      \"name\": \"Giảm 20% tháng 1\",\n" +
                     "      \"startDate\": \"2024-01-01\",\n" +
                     "      \"endDate\": \"2024-01-31\"\n" +
                     "    }\n" +
                     "  ]\n" +
                     "}",
        example = "{\"policies\": {\"returnPolicy\": \"Đổi trả trong 7 ngày\"}, \"promotions\": []}"
    )
    String metadata;

    @Schema(
        description = "Style dạng JSON - Lưu style mong muốn của cửa hàng:\n" +
                     "- Tone: giọng điệu giao tiếp (thân thiện, chuyên nghiệp, vui vẻ...)\n" +
                     "- Color: màu sắc chủ đạo\n" +
                     "- Font: font chữ\n" +
                     "- Language: ngôn ngữ\n\n" +
                     "Ví dụ:\n" +
                     "{\n" +
                     "  \"tone\": \"thân thiện\",\n" +
                     "  \"color\": {\n" +
                     "    \"primary\": \"#FF6B6B\",\n" +
                     "    \"secondary\": \"#4ECDC4\"\n" +
                     "  },\n" +
                     "  \"font\": \"Arial\",\n" +
                     "  \"language\": \"vi\"\n" +
                     "}",
        example = "{\"tone\": \"thân thiện\", \"color\": {\"primary\": \"#FF6B6B\"}, \"language\": \"vi\"}"
    )
    String style;
}

