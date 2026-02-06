package miniapp.com.vn.minichatbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniapp.com.vn.minichatbackend.common.ErrorCode;
import miniapp.com.vn.minichatbackend.common.Result;
import miniapp.com.vn.minichatbackend.dto.request.CreateProductRequest;
import miniapp.com.vn.minichatbackend.dto.request.UpdateProductRequest;
import miniapp.com.vn.minichatbackend.dto.response.ProductManagementResponse;
import miniapp.com.vn.minichatbackend.entity.BackOfficeBusiness;
import miniapp.com.vn.minichatbackend.entity.Product;
import miniapp.com.vn.minichatbackend.repo.BackOfficeBusinessRepository;
import miniapp.com.vn.minichatbackend.repo.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductManagementService {

    private final ProductRepository productRepository;
    private final BackOfficeBusinessRepository backOfficeBusinessRepository;

    /**
     * Kiểm tra user có quyền truy cập business (có trong bảng BackOffice_Business) không.
     * @return null nếu có quyền, otherwise AccessCheck với errorCode và message
     */
    private AccessCheck failureIfNoAccessToBusiness(Long backOfficeUserId, Long businessId) {
        if (backOfficeUserId == null) {
            return new AccessCheck(ErrorCode.UNAUTHORIZED, "Unauthorized");
        }
        boolean hasAccess = backOfficeBusinessRepository
                .findByBackOfficeUserIdAndBusinessId(backOfficeUserId, businessId)
                .isPresent();
        if (!hasAccess) {
            return new AccessCheck(ErrorCode.FORBIDDEN, "Bạn không có quyền truy cập business này");
        }
        return null;
    }

    private record AccessCheck(ErrorCode errorCode, String message) {}

    @Transactional
    public Result<ProductManagementResponse> createProduct(CreateProductRequest request, Long backOfficeUserId) {
        try {
            if (backOfficeUserId == null) {
                return Result.error(ErrorCode.UNAUTHORIZED, "Unauthorized");
            }
            Long businessId = request.getBusinessId();
            if (businessId == null) {
                return Result.error(ErrorCode.INVALID_REQUEST, "businessId là bắt buộc");
            }
            AccessCheck noAccess = failureIfNoAccessToBusiness(backOfficeUserId, businessId);
            if (noAccess != null) {
                return Result.error(noAccess.errorCode(), noAccess.message());
            }
            Product product = Product.builder()
                    .businessId(businessId)
                    .name(request.getName())
                    .description(request.getDescription())
                    .price(request.getPrice())
                    .mainImageUrl(request.getMainImageUrl())
                    .detailImageUrl(request.getDetailImageUrl())
                    .quantityAvail(request.getQuantityAvail() != null ? request.getQuantityAvail() : 0)
                    .status(request.getStatus() != null ? request.getStatus() : "1")
                    .metadata(request.getMetadata())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Product savedProduct = productRepository.save(product);
            log.info("Product created successfully: id={}, name={}, businessId={}, backOfficeUserId={}",
                    savedProduct.getId(), savedProduct.getName(), savedProduct.getBusinessId(), backOfficeUserId);

            return Result.success(toResponse(savedProduct));
        } catch (Exception e) {
            log.error("Error creating product: {}", e.getMessage(), e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi tạo sản phẩm: " + e.getMessage());
        }
    }

    @Transactional
    public Result<ProductManagementResponse> updateProduct(Long productId, UpdateProductRequest request, Long backOfficeUserId) {
        try {
            Product product = productRepository.findById(productId)
                    .orElse(null);

            if (product == null) {
                return Result.error(ErrorCode.NOT_FOUND, "Không tìm thấy sản phẩm với id: " + productId);
            }
            AccessCheck noAccess = failureIfNoAccessToBusiness(backOfficeUserId, product.getBusinessId());
            if (noAccess != null) {
                return Result.error(noAccess.errorCode(), noAccess.message());
            }
            // Nếu đổi businessId thì phải thuộc business user có quyền
            if (request.getBusinessId() != null && !request.getBusinessId().equals(product.getBusinessId())) {
                AccessCheck noAccessNewBiz = failureIfNoAccessToBusiness(backOfficeUserId, request.getBusinessId());
                if (noAccessNewBiz != null) {
                    return Result.error(noAccessNewBiz.errorCode(), noAccessNewBiz.message());
                }
                product.setBusinessId(request.getBusinessId());
            } else if (request.getBusinessId() != null) {
                product.setBusinessId(request.getBusinessId());
            }
            if (request.getName() != null) {
                product.setName(request.getName());
            }
            if (request.getDescription() != null) {
                product.setDescription(request.getDescription());
            }
            if (request.getPrice() != null) {
                product.setPrice(request.getPrice());
            }
            if (request.getMainImageUrl() != null) {
                product.setMainImageUrl(request.getMainImageUrl());
            }
            if (request.getDetailImageUrl() != null) {
                product.setDetailImageUrl(request.getDetailImageUrl());
            }
            if (request.getQuantityAvail() != null) {
                product.setQuantityAvail(request.getQuantityAvail());
            }
            if (request.getStatus() != null) {
                product.setStatus(request.getStatus());
            }
            if (request.getMetadata() != null) {
                product.setMetadata(request.getMetadata());
            }
            product.setUpdatedAt(LocalDateTime.now());

            Product updatedProduct = productRepository.save(product);
            log.info("Product updated successfully: id={}, name={}", updatedProduct.getId(), updatedProduct.getName());

            return Result.success(toResponse(updatedProduct));
        } catch (Exception e) {
            log.error("Error updating product: {}", e.getMessage(), e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi cập nhật sản phẩm: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Result<ProductManagementResponse> getProductById(Long productId, Long backOfficeUserId) {
        try {
            Product product = productRepository.findById(productId)
                    .orElse(null);

            if (product == null) {
                return Result.error(ErrorCode.NOT_FOUND, "Không tìm thấy sản phẩm với id: " + productId);
            }
            AccessCheck noAccess = failureIfNoAccessToBusiness(backOfficeUserId, product.getBusinessId());
            if (noAccess != null) {
                return Result.error(noAccess.errorCode(), noAccess.message());
            }
            return Result.success(toResponse(product));
        } catch (Exception e) {
            log.error("Error getting product by id: {}", e.getMessage(), e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi lấy thông tin sản phẩm: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Result<List<ProductManagementResponse>> getAllProducts(Long backOfficeUserId) {
        try {
            if (backOfficeUserId == null) {
                return Result.error(ErrorCode.UNAUTHORIZED, "Unauthorized");
            }
            List<BackOfficeBusiness> userBusinessLinks = backOfficeBusinessRepository.findByBackOfficeUserId(backOfficeUserId);
            List<Long> businessIds = userBusinessLinks.stream()
                    .map(BackOfficeBusiness::getBusinessId)
                    .collect(Collectors.toList());
            if (businessIds.isEmpty()) {
                return Result.success(Collections.emptyList());
            }
            List<Product> products = productRepository.findByBusinessIdIn(businessIds);
            List<ProductManagementResponse> responses = products.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());

            return Result.success(responses);
        } catch (Exception e) {
            log.error("Error getting all products: {}", e.getMessage(), e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi lấy danh sách sản phẩm: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Result<List<ProductManagementResponse>> getProductsByBusinessId(Long businessId, Long backOfficeUserId) {
        try {
            AccessCheck noAccess = failureIfNoAccessToBusiness(backOfficeUserId, businessId);
            if (noAccess != null) {
                return Result.error(noAccess.errorCode(), noAccess.message());
            }
            List<Product> products = productRepository.findByBusinessId(businessId);
            List<ProductManagementResponse> responses = products.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());

            return Result.success(responses);
        } catch (Exception e) {
            log.error("Error getting products by business id: {}", e.getMessage(), e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi lấy danh sách sản phẩm theo business: " + e.getMessage());
        }
    }

    @Transactional
    public Result<Void> deleteProduct(Long productId, Long backOfficeUserId) {
        try {
            Product product = productRepository.findById(productId)
                    .orElse(null);

            if (product == null) {
                return Result.error(ErrorCode.NOT_FOUND, "Không tìm thấy sản phẩm với id: " + productId);
            }
            AccessCheck noAccess = failureIfNoAccessToBusiness(backOfficeUserId, product.getBusinessId());
            if (noAccess != null) {
                return Result.error(noAccess.errorCode(), noAccess.message());
            }
            productRepository.delete(product);
            log.info("Product deleted successfully: id={}", productId);

            return Result.success(null);
        } catch (Exception e) {
            log.error("Error deleting product: {}", e.getMessage(), e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi xóa sản phẩm: " + e.getMessage());
        }
    }

    private ProductManagementResponse toResponse(Product product) {
        return ProductManagementResponse.builder()
                .id(product.getId())
                .businessId(product.getBusinessId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .mainImageUrl(product.getMainImageUrl())
                .detailImageUrl(product.getDetailImageUrl())
                .quantityAvail(product.getQuantityAvail())
                .status(product.getStatus())
                .metadata(product.getMetadata())
                .build();
    }
}

