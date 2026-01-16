package miniapp.com.vn.minichatbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "Request for uploading an image")
public class ImageUploadRequest {
    
    @Schema(description = "The image file to upload", required = true)
    private MultipartFile file;
    
    @Schema(description = "Optional folder name in Cloudinary", example = "foodshare/products")
    private String folder;
    
    @Schema(description = "Optional public ID for the image", example = "product_123")
    private String publicId;
    
    @Schema(description = "Optional transformation parameters", example = "w_500,h_500,c_fill")
    private String transformation;
}
