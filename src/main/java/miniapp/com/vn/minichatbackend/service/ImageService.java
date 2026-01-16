package miniapp.com.vn.minichatbackend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniapp.com.vn.minichatbackend.common.ErrorCode;
import miniapp.com.vn.minichatbackend.common.Result;
import miniapp.com.vn.minichatbackend.dto.request.ImageUploadRequest;
import miniapp.com.vn.minichatbackend.dto.response.ImageUploadResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final Cloudinary cloudinary;

    public Result<ImageUploadResponse> uploadImage(ImageUploadRequest request) {
        MultipartFile file = request.getFile();
        
        if (file == null || file.isEmpty()) {
            return Result.error(ErrorCode.INVALID_REQUEST, "File cannot be empty");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error(ErrorCode.INVALID_FILE_TYPE);
        }

        // Validate file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.error(ErrorCode.FILE_TOO_LARGE);
        }

        try {
            // Prepare upload parameters
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                "folder", request.getFolder() != null ? request.getFolder() : "minichat",
                "public_id", request.getPublicId(),
                "transformation", request.getTransformation()
            );

            // Upload to Cloudinary
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                file.getBytes(), 
                uploadParams
            );

            // Build response
            ImageUploadResponse response = ImageUploadResponse.builder()
                .publicId((String) uploadResult.get("public_id"))
                .secureUrl((String) uploadResult.get("secure_url"))
                .originalFilename((String) uploadResult.get("original_filename"))
                .format((String) uploadResult.get("format"))
                .bytes((Integer) uploadResult.get("bytes"))
                .width((Integer) uploadResult.get("width"))
                .height((Integer) uploadResult.get("height"))
                .resourceType((String) uploadResult.get("resource_type"))
                .build();

            return Result.success(response);

        } catch (Exception e) {
            log.info("uploadImage ex: ", e);
            return Result.error(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    public Result<String> deleteImage(String publicId) {
        try {
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            return Result.success("Image deleted successfully");
        } catch (Exception e) {
            log.info("deleteImage ex: ", e);
            return Result.error(ErrorCode.FILE_DELETE_FAILED);
        }
    }

    public Result<String> generateImageUrl(String publicId, String transformation) {
        try {
            if (transformation != null && !transformation.isEmpty()) {
                // Create Transformation object and add raw transformation
                Transformation t = new Transformation();
                t.rawTransformation(transformation);
                String url = cloudinary.url().publicId(publicId).transformation(t).generate();
                return Result.success(url);
            }
            String url = cloudinary.url().publicId(publicId).generate();
            return Result.success(url);
        } catch (Exception e) {
            log.info("generateImageUrl ex: ", e);
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }
}
