package miniapp.com.vn.minichatbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniapp.com.vn.minichatbackend.common.Result;
import miniapp.com.vn.minichatbackend.dto.request.CreateProductRequest;
import miniapp.com.vn.minichatbackend.dto.request.UpdateProductRequest;
import miniapp.com.vn.minichatbackend.dto.response.ProductManagementResponse;
import miniapp.com.vn.minichatbackend.service.ProductManagementService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static miniapp.com.vn.minichatbackend.common.ErrorCode.INVALID_CREDENTIALS;

@Slf4j
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "API quản lý Product")
public class ProductManagementController {

    private final ProductManagementService productManagementService;

    /**
     * Lấy thông tin user hiện tại
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null) {
            // EndpointAuthFilter set Principal là Integer
            if (auth.getPrincipal() instanceof Integer) {
                return ((Integer) auth.getPrincipal()).longValue();
            }
            // Nếu là Long thì cast trực tiếp
            if (auth.getPrincipal() instanceof Long) {
                return (Long) auth.getPrincipal();
            }
        }
        return null;
    }

    // =====================================================
    // PRODUCT MANAGEMENT APIs
    // =====================================================

    /**
     * Tạo sản phẩm mới
     */
    @PostMapping
    @Operation(
            summary = "Tạo sản phẩm mới",
            description = "Tạo sản phẩm mới"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo sản phẩm thành công",
                    content = @Content(schema = @Schema(implementation = ProductManagementResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<ProductManagementResponse> createProduct(
            @Parameter(description = "Thông tin sản phẩm mới", required = true)
            @Valid @RequestBody CreateProductRequest request
    ) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(INVALID_CREDENTIALS, "Unauthorized");
        }

        log.info("Create product request: userId={}, name={}, businessId={}", 
                userId, request.getName(), request.getBusinessId());
        return productManagementService.createProduct(request, userId);
    }

    /**
     * Cập nhật sản phẩm
     */
    @PutMapping("/{productId}")
    @Operation(
            summary = "Cập nhật sản phẩm",
            description = "Cập nhật thông tin sản phẩm"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(schema = @Schema(implementation = ProductManagementResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<ProductManagementResponse> updateProduct(
            @Parameter(description = "ID của sản phẩm", example = "1", required = true)
            @PathVariable Long productId,
            @Parameter(description = "Thông tin cập nhật", required = true)
            @Valid @RequestBody UpdateProductRequest request
    ) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(INVALID_CREDENTIALS, "Unauthorized");
        }

        log.info("Update product request: userId={}, productId={}", userId, productId);
        return productManagementService.updateProduct(productId, request, userId);
    }

    /**
     * Lấy danh sách tất cả sản phẩm
     */
    @GetMapping
    @Operation(
            summary = "Lấy danh sách sản phẩm",
            description = "Lấy danh sách tất cả sản phẩm"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(schema = @Schema(implementation = ProductManagementResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<List<ProductManagementResponse>> getAllProducts() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(INVALID_CREDENTIALS, "Unauthorized");
        }

        log.info("Get all products request: userId={}", userId);
        return productManagementService.getAllProducts(userId);
    }

    /**
     * Lấy danh sách sản phẩm theo business ID
     */
    @GetMapping("/business/{businessId}")
    @Operation(
            summary = "Lấy danh sách sản phẩm theo business",
            description = "Lấy danh sách sản phẩm của một business"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(schema = @Schema(implementation = ProductManagementResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<List<ProductManagementResponse>> getProductsByBusinessId(
            @Parameter(description = "ID của business", example = "1", required = true)
            @PathVariable Long businessId
    ) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(INVALID_CREDENTIALS, "Unauthorized");
        }

        log.info("Get products by business id request: userId={}, businessId={}", userId, businessId);
        return productManagementService.getProductsByBusinessId(businessId, userId);
    }

    /**
     * Lấy thông tin sản phẩm theo ID
     */
    @GetMapping("/{productId}")
    @Operation(
            summary = "Lấy thông tin sản phẩm",
            description = "Lấy thông tin sản phẩm theo ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công",
                    content = @Content(schema = @Schema(implementation = ProductManagementResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<ProductManagementResponse> getProduct(
            @Parameter(description = "ID của sản phẩm", example = "1", required = true)
            @PathVariable Long productId
    ) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(INVALID_CREDENTIALS, "Unauthorized");
        }

        log.info("Get product request: userId={}, productId={}", userId, productId);
        return productManagementService.getProductById(productId, userId);
    }

    /**
     * Xóa sản phẩm
     */
    @DeleteMapping("/{productId}")
    @Operation(
            summary = "Xóa sản phẩm",
            description = "Xóa sản phẩm theo ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<Void> deleteProduct(
            @Parameter(description = "ID của sản phẩm", example = "1", required = true)
            @PathVariable Long productId
    ) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(INVALID_CREDENTIALS, "Unauthorized");
        }

        log.info("Delete product request: userId={}, productId={}", userId, productId);
        return productManagementService.deleteProduct(productId, userId);
    }
}

