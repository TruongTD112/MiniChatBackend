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
import miniapp.com.vn.minichatbackend.dto.request.CreateShopRequest;
import miniapp.com.vn.minichatbackend.dto.response.ShopManagementResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static miniapp.com.vn.minichatbackend.common.ErrorCode.INVALID_CREDENTIALS;


@Slf4j
@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
@Tag(name = "Seller Management", description = "API quản lý cho Seller - chỉ quản lý sản phẩm và cửa hàng của mình")
public class OwnerManagementController {

    private final ProductManagementService productManagementService;
    private final ShopManagementService shopManagementService;
    private final OrderService orderService;

    /**
     * Lấy thông tin seller hiện tại
     */
    private Integer getCurrentSellerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Integer) {
            return (Integer) auth.getPrincipal();
        }
        return null;
    }

    // =====================================================
    // SHOP MANAGEMENT APIs
    // =====================================================

    /**
     * Tạo cửa hàng mới (chỉ seller mới tạo được)
     */
    @PostMapping("/shops")
    @Operation(
            summary = "Tạo cửa hàng mới",
            description = "Seller tạo cửa hàng mới cho mình"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo cửa hàng thành công",
                    content = @Content(schema = @Schema(implementation = ShopManagementResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<ShopManagementResponse> createShop(
            @Parameter(description = "Thông tin cửa hàng mới", required = true)
            @Valid @RequestBody CreateShopRequest request
    ) {
        Integer sellerId = getCurrentSellerId();
        if (sellerId == null) {
            return Result.error(INVALID_CREDENTIALS, "Unauthorized");
        }

        log.info("Seller create shop request: sellerId={}, name={}", sellerId, request.getName());
        return shopManagementService.createShop(request, sellerId);
    }

    /**
     * Cập nhật cửa hàng của seller
     */
    @PutMapping("/shops/{shopId}")
    @Operation(
            summary = "Cập nhật cửa hàng",
            description = "Seller cập nhật thông tin cửa hàng của mình"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(schema = @Schema(implementation = ShopManagementResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập cửa hàng này"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy cửa hàng"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<ShopManagementResponse> updateShop(
            @Parameter(description = "ID của cửa hàng", example = "1", required = true)
            @PathVariable Integer shopId,
            @Parameter(description = "Thông tin cập nhật", required = true)
            @Valid @RequestBody UpdateShopRequest request
    ) {
        Integer sellerId = getCurrentSellerId();
        if (sellerId == null) {
            return Result.error(INVALID_CREDENTIALS, "Unauthorized");
        }

        // TODO: Kiểm tra seller có quyền truy cập shop này không
        // Cần implement logic kiểm tra ShopMember table

        log.info("Seller update shop request: sellerId={}, shopId={}", sellerId, shopId);
        return shopManagementService.updateShop(shopId, request);
    }

    /**
     * Lấy danh sách cửa hàng của seller
     */
    @GetMapping("/shops")
    @Operation(
            summary = "Lấy danh sách cửa hàng của seller",
            description = "Seller xem danh sách tất cả cửa hàng mà mình có quyền quản lý"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(schema = @Schema(implementation = ShopManagementResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<List<ShopManagementResponse>> getMyShops() {
        Integer sellerId = getCurrentSellerId();
        if (sellerId == null) {
            return Result.error(INVALID_CREDENTIALS, "Unauthorized");
        }

        log.info("Seller get my shops request: sellerId={}", sellerId);
        return shopManagementService.getShopsBySellerId(sellerId);
    }

    /**
     * Lấy thông tin cửa hàng của seller
     */
    @GetMapping("/shops/{shopId}")
    @Operation(
            summary = "Lấy thông tin cửa hàng",
            description = "Seller xem thông tin cửa hàng của mình"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công",
                    content = @Content(schema = @Schema(implementation = ShopManagementResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập cửa hàng này"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy cửa hàng"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<ShopManagementResponse> getShop(
            @Parameter(description = "ID của cửa hàng", example = "1", required = true)
            @PathVariable Integer shopId
    ) {
        Integer sellerId = getCurrentSellerId();
        if (sellerId == null) {
            return Result.error(INVALID_CREDENTIALS, "Unauthorized");
        }

        // TODO: Kiểm tra seller có quyền truy cập shop này không

        log.info("Seller get shop request: sellerId={}, shopId={}", sellerId, shopId);
        return shopManagementService.getShopById(shopId);
    }

    // =====================================================
    // PRODUCT MANAGEMENT APIs
    // =====================================================

    /**
     * Tạo sản phẩm mới cho shop của seller
     */
    @PostMapping("/products")
    @Operation(
            summary = "Tạo sản phẩm mới",
            description = "Seller tạo sản phẩm mới cho shop của mình"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo sản phẩm thành công",
                    content = @Content(schema = @Schema(implementation = ProductManagementResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập shop này"),
            @ApiResponse(responseCode = "404", description = "Shop không tồn tại"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<ProductManagementResponse> createProduct(
            @Parameter(description = "Thông tin sản phẩm mới", required = true)
            @Valid @RequestBody CreateProductRequest request
    ) {
        Integer sellerId = getCurrentSellerId();
        if (sellerId == null) {
            return Result.error(INVALID_CREDENTIALS, "Unauthorized");
        }

        // TODO: Kiểm tra seller có quyền truy cập shop này không

        log.info("Seller create product request: sellerId={}, shopId={}, name={}",
                sellerId, request.getShopId(), request.getName());
        return productManagementService.createProduct(request);
    }

    /**
     * Cập nhật sản phẩm của seller
     */
    @PutMapping("/products/{productId}")
    @Operation(
            summary = "Cập nhật sản phẩm",
            description = "Seller cập nhật thông tin sản phẩm của mình"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(schema = @Schema(implementation = ProductManagementResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập sản phẩm này"),
            @ApiResponse(responseCode = "404", description = "Sản phẩm không tồn tại"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<ProductManagementResponse> updateProduct(
            @Parameter(description = "ID của sản phẩm", example = "1", required = true)
            @PathVariable Integer productId,
            @Parameter(description = "Thông tin cập nhật", required = true)
            @Valid @RequestBody UpdateProductRequest request
    ) {
        Integer sellerId = getCurrentSellerId();
        if (sellerId == null) {
            return Result.error(INVALID_CREDENTIALS, "Unauthorized");
        }

        // TODO: Kiểm tra seller có quyền truy cập sản phẩm này không

        log.info("Seller update product request: sellerId={}, productId={}", sellerId, productId);
        return productManagementService.updateProduct(productId, request);
    }

    /**
     * Xóa sản phẩm của seller
     */
    @DeleteMapping("/products/{productId}")
    @Operation(
            summary = "Xóa sản phẩm",
            description = "Seller xóa sản phẩm của mình"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập sản phẩm này"),
            @ApiResponse(responseCode = "404", description = "Sản phẩm không tồn tại"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<Void> deleteProduct(
            @Parameter(description = "ID của sản phẩm", example = "1", required = true)
            @PathVariable Integer productId
    ) {
        Integer sellerId = getCurrentSellerId();
        if (sellerId == null) {
            return Result.error(INVALID_CREDENTIALS, "Unauthorized");
        }

        // TODO: Kiểm tra seller có quyền truy cập sản phẩm này không

        log.info("Seller delete product request: sellerId={}, productId={}", sellerId, productId);
        return productManagementService.deleteProduct(productId);
    }

    /**
     * Lấy thông tin sản phẩm của seller
     */
    @GetMapping("/products/{productId}")
    @Operation(
            summary = "Lấy thông tin sản phẩm",
            description = "Seller xem thông tin sản phẩm của mình"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công",
                    content = @Content(schema = @Schema(implementation = ProductManagementResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập sản phẩm này"),
            @ApiResponse(responseCode = "404", description = "Sản phẩm không tồn tại"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<ProductManagementResponse> getProduct(
            @Parameter(description = "ID của sản phẩm", example = "1", required = true)
            @PathVariable Integer productId
    ) {
        Integer sellerId = getCurrentSellerId();
        if (sellerId == null) {
            return Result.error(INVALID_CREDENTIALS, "Unauthorized");
        }

        // TODO: Kiểm tra seller có quyền truy cập sản phẩm này không

        log.info("Seller get product request: sellerId={}, productId={}", sellerId, productId);
        return productManagementService.getProductById(productId);
    }

    /**
     * Lấy danh sách sản phẩm của shop
     */
    @GetMapping("/shops/{shopId}/products")
    @Operation(
            summary = "Lấy danh sách sản phẩm của shop",
            description = "Seller xem danh sách sản phẩm của shop của mình"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(schema = @Schema(implementation = ProductManagementResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập shop này"),
            @ApiResponse(responseCode = "404", description = "Shop không tồn tại"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<PagedResult<ProductManagementResponse>> getProductsByShop(
            @Parameter(description = "ID của shop", example = "1", required = true)
            @PathVariable Integer shopId,
            @Parameter(description = "Trang hiện tại (bắt đầu từ 0)")
            @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Kích thước trang")
            @RequestParam(name = "size", required = false, defaultValue = "20") Integer size,
            @Parameter(description = "Trường sắp xếp")
            @RequestParam(name = "sortBy", required = false, defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Chiều sắp xếp: asc|desc")
            @RequestParam(name = "sortDirection", required = false, defaultValue = "desc") String sortDirection
    ) {
        Integer sellerId = getCurrentSellerId();
        if (sellerId == null) {
            return Result.error(INVALID_CREDENTIALS, "Unauthorized");
        }

        // TODO: Kiểm tra seller có quyền truy cập shop này không

        log.info("Seller get products by shop request: sellerId={}, shopId={}, page={}, size={}, sortBy={}, sortDirection={}",
                sellerId, shopId, page, size, sortBy, sortDirection);
        return productManagementService.getProductsByShopIdPaged(shopId, page, size, sortBy, sortDirection);
    }

    // =====================================================
    // ORDER MANAGEMENT APIs
    // =====================================================

    /**
     * Lấy danh sách đơn hàng theo shop
     */
    @GetMapping("/orders/shop")
    @Operation(
            summary = "Lấy danh sách đơn hàng theo shop",
            description = "Seller xem danh sách đơn hàng của shop của mình"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(schema = @Schema(implementation = ShopOrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập shop này"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<PagedResult<ShopOrderResponse>> getOrdersByShop(
            @Parameter(description = "Tham số lọc đơn hàng")
            @Valid ShopOrderListRequest request
    ) {
        Integer sellerId = getCurrentSellerId();
        if (sellerId == null) {
            return Result.error(INVALID_CREDENTIALS, "Unauthorized");
        }

        log.info("Seller get orders by shop request: sellerId={}, shopId={}, status={}",
                sellerId, request.getShopId(), request.getStatus());

        Result<PagedResult<ShopOrderResponse>> result = orderService.getOrdersByShop(sellerId, request);

        if (result.isSuccess()) {
            log.info("Shop orders retrieved successfully: sellerId={}, shopId={}, count={}", 
                    sellerId, request.getShopId(), result.getData().getTotalElements());
        } else {
            log.warn("Shop orders retrieval failed: sellerId={}, shopId={}, code={}, message={}", 
                    sellerId, request.getShopId(), result.getCode(), result.getMessage());
        }
        return result;
    }

    /**
     * Cập nhật trạng thái đơn hàng
     */
    @PutMapping("/orders/{orderId}/status")
    @Operation(
            summary = "Cập nhật trạng thái đơn hàng",
            description = "Seller cập nhật trạng thái đơn hàng của shop mình"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(schema = @Schema(implementation = ShopOrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền cập nhật đơn hàng này"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đơn hàng"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<ShopOrderResponse> updateOrderStatus(
            @Parameter(description = "ID của đơn hàng", required = true)
            @PathVariable Integer orderId,
            @Parameter(description = "Thông tin cập nhật trạng thái", required = true)
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        Integer sellerId = getCurrentSellerId();
        if (sellerId == null) {
            return Result.error(INVALID_CREDENTIALS, "Unauthorized");
        }

        log.info("Seller update order status request: sellerId={}, orderId={}, newStatus={}", 
                sellerId, orderId, request.getStatus());

        Result<ShopOrderResponse> result = orderService.updateOrderStatusForSeller(orderId, request, sellerId);

        if (result.isSuccess()) {
            log.info("Order status updated successfully by seller: sellerId={}, orderId={}, newStatus={}", 
                    sellerId, orderId, request.getStatus());
        } else {
            log.warn("Order status update failed by seller: sellerId={}, orderId={}, newStatus={}, code={}, message={}", 
                    sellerId, orderId, request.getStatus(), result.getCode(), result.getMessage());
        }
        return result;
    }

}
