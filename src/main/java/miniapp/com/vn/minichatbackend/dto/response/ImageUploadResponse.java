package miniapp.com.vn.minichatbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response after successful image upload")
public class ImageUploadResponse {
    
    @Schema(description = "Public ID of the uploaded image", example = "foodshare/products/product_123")
    private String publicId;
    
    @Schema(description = "Secure URL of the uploaded image", example = "https://res.cloudinary.com/your-cloud/image/upload/v1234567890/foodshare/products/product_123.jpg")
    private String secureUrl;
    
    @Schema(description = "Original filename", example = "product_image.jpg")
    private String originalFilename;
    
    @Schema(description = "File format", example = "jpg")
    private String format;
    
    @Schema(description = "File size in bytes", example = "1024000")
    private Integer bytes;
    
    @Schema(description = "Image width in pixels", example = "1920")
    private Integer width;
    
    @Schema(description = "Image height in pixels", example = "1080")
    private Integer height;
    
    @Schema(description = "Cloudinary resource type", example = "image")
    private String resourceType;
}
