package miniapp.com.vn.minichatbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class CreateShopRequest {
    @NotBlank(message = "Tên cửa hàng không được để trống")
    @Size(max = 255, message = "Tên cửa hàng không được vượt quá 255 ký tự")
    String name;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    String address;

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    String phone;

    @Size(max = 255, message = "URL ảnh không được vượt quá 255 ký tự")
    String imageUrl;

    String description;

    @NotBlank(message = "Trạng thái không được để trống")
    @io.swagger.v3.oas.annotations.media.Schema(
        description = "Trạng thái cửa hàng: 1 (ACTIVE), 0 (INACTIVE)",
        example = "1",
        allowableValues = {"1", "0"}
    )
    String status;
}