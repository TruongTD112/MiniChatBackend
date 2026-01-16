package miniapp.com.vn.minichatbackend.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import miniapp.com.vn.minichatbackend.common.Result;
import miniapp.com.vn.minichatbackend.dto.request.ImageUploadRequest;
import miniapp.com.vn.minichatbackend.dto.response.ImageUploadResponse;
import miniapp.com.vn.minichatbackend.service.ImageService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Tag(name = "Image Management", description = "APIs for image upload and management")
public class ImageController {

    private final ImageService imageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload an image to Cloudinary",
            description = "Upload an image file to Cloudinary cloud storage with optional parameters"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Image uploaded successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ImageUploadResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid file or request parameters"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Result<ImageUploadResponse> uploadImage(
            @Parameter(description = "Image file to upload", required = true)
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "Optional folder name in Cloudinary")
            @RequestParam(value = "folder", required = false) String folder,

            @Parameter(description = "Optional public ID for the image")
            @RequestParam(value = "publicId", required = false) String publicId,

            @Parameter(description = "Optional transformation parameters")
            @RequestParam(value = "transformation", required = false) String transformation) {

        ImageUploadRequest request = new ImageUploadRequest();
        request.setFile(file);
        request.setFolder(folder);
        request.setPublicId(publicId);
        request.setTransformation(transformation);

        return imageService.uploadImage(request);
    }

    @DeleteMapping("/{publicId}")
    @Operation(
            summary = "Delete an image from Cloudinary",
            description = "Delete an image from Cloudinary using its public ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Image not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Result<String> deleteImage(
            @Parameter(description = "Public ID of the image to delete", required = true)
            @PathVariable String publicId) {

        return imageService.deleteImage(publicId);
    }

    @GetMapping("/url/{publicId}")
    @Operation(
            summary = "Generate image URL",
            description = "Generate a Cloudinary URL for an image with optional transformations"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image URL generated successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Result<String> generateImageUrl(
            @Parameter(description = "Public ID of the image", required = true)
            @PathVariable String publicId,

            @Parameter(description = "Optional transformation parameters")
            @RequestParam(value = "transformation", required = false) String transformation) {

        return imageService.generateImageUrl(publicId, transformation);
    }
}
