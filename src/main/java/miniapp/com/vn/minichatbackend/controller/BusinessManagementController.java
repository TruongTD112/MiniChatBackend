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
import miniapp.com.vn.minichatbackend.dto.request.CreateBusinessRequest;
import miniapp.com.vn.minichatbackend.dto.request.UpdateBusinessRequest;
import miniapp.com.vn.minichatbackend.dto.response.BusinessManagementResponse;
import miniapp.com.vn.minichatbackend.service.BusinessManagementService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static miniapp.com.vn.minichatbackend.common.ErrorCode.INVALID_CREDENTIALS;

@Slf4j
@RestController
@RequestMapping("/api/business")
@RequiredArgsConstructor
@Tag(name = "Business Management", description = "API quản lý Business")
public class BusinessManagementController {

    private final BusinessManagementService businessManagementService;

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
    // BUSINESS MANAGEMENT APIs
    // =====================================================

    /**
     * Tạo business mới
     */
    @PostMapping
    @Operation(
            summary = "Tạo business mới",
            description = "Tạo business mới"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo business thành công",
                    content = @Content(schema = @Schema(implementation = BusinessManagementResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<BusinessManagementResponse> createBusiness(
            @Parameter(description = "Thông tin business mới", required = true)
            @Valid @RequestBody CreateBusinessRequest request
    ) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(INVALID_CREDENTIALS, "Unauthorized");
        }

        log.info("Create business request: userId={}, name={}", userId, request.getName());
        return businessManagementService.createBusiness(request, userId);
    }

    /**
     * Cập nhật business
     */
    @PutMapping("/{businessId}")
    @Operation(
            summary = "Cập nhật business",
            description = "Cập nhật thông tin business"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(schema = @Schema(implementation = BusinessManagementResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy business"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<BusinessManagementResponse> updateBusiness(
            @Parameter(description = "ID của business", example = "1", required = true)
            @PathVariable Long businessId,
            @Parameter(description = "Thông tin cập nhật", required = true)
            @Valid @RequestBody UpdateBusinessRequest request
    ) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(INVALID_CREDENTIALS, "Unauthorized");
        }

        log.info("Update business request: userId={}, businessId={}", userId, businessId);
        return businessManagementService.updateBusiness(businessId, request, userId);
    }

    /**
     * Lấy danh sách tất cả business
     */
    @GetMapping
    @Operation(
            summary = "Lấy danh sách business",
            description = "Lấy danh sách tất cả business"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(schema = @Schema(implementation = BusinessManagementResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<List<BusinessManagementResponse>> getAllBusinesses() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(INVALID_CREDENTIALS, "Unauthorized");
        }

        log.info("Get all businesses request: userId={}", userId);
        return businessManagementService.getAllBusinesses(userId);
    }

    /**
     * Lấy thông tin business theo ID
     */
    @GetMapping("/{businessId}")
    @Operation(
            summary = "Lấy thông tin business",
            description = "Lấy thông tin business theo ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công",
                    content = @Content(schema = @Schema(implementation = BusinessManagementResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy business"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<BusinessManagementResponse> getBusiness(
            @Parameter(description = "ID của business", example = "1", required = true)
            @PathVariable Long businessId
    ) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(INVALID_CREDENTIALS, "Unauthorized");
        }

        log.info("Get business request: userId={}, businessId={}", userId, businessId);
        return businessManagementService.getBusinessById(businessId, userId);
    }

    /**
     * Xóa business
     */
    @DeleteMapping("/{businessId}")
    @Operation(
            summary = "Xóa business",
            description = "Xóa business theo ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy business"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<Void> deleteBusiness(
            @Parameter(description = "ID của business", example = "1", required = true)
            @PathVariable Long businessId
    ) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(INVALID_CREDENTIALS, "Unauthorized");
        }

        log.info("Delete business request: userId={}, businessId={}", userId, businessId);
        return businessManagementService.deleteBusiness(businessId, userId);
    }

}
