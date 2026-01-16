package miniapp.com.vn.minichatbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniapp.com.vn.minichatbackend.common.ErrorCode;
import miniapp.com.vn.minichatbackend.common.Result;
import miniapp.com.vn.minichatbackend.dto.request.CreateProductRequest;
import miniapp.com.vn.minichatbackend.dto.request.UpdateProductRequest;
import miniapp.com.vn.minichatbackend.dto.response.ProductManagementResponse;
import miniapp.com.vn.minichatbackend.entity.Product;
import miniapp.com.vn.minichatbackend.repo.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductManagementService {

    private final ProductRepository productRepository;

    @Transactional
    public Result<ProductManagementResponse> createProduct(CreateProductRequest request) {
        try {
            Product product = Product.builder()
                    .businessId(request.getBusinessId())
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
            log.info("Product created successfully: id={}, name={}, businessId={}", 
                    savedProduct.getId(), savedProduct.getName(), savedProduct.getBusinessId());

            return Result.success(toResponse(savedProduct));
        } catch (Exception e) {
            log.error("Error creating product: {}", e.getMessage(), e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi tạo sản phẩm: " + e.getMessage());
        }
    }

    @Transactional
    public Result<ProductManagementResponse> updateProduct(Long productId, UpdateProductRequest request) {
        try {
            Product product = productRepository.findById(productId)
                    .orElse(null);

            if (product == null) {
                return Result.error(ErrorCode.NOT_FOUND, "Không tìm thấy sản phẩm với id: " + productId);
            }

            if (request.getBusinessId() != null) {
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
    public Result<ProductManagementResponse> getProductById(Long productId) {
        try {
            Product product = productRepository.findById(productId)
                    .orElse(null);

            if (product == null) {
                return Result.error(ErrorCode.NOT_FOUND, "Không tìm thấy sản phẩm với id: " + productId);
            }

            return Result.success(toResponse(product));
        } catch (Exception e) {
            log.error("Error getting product by id: {}", e.getMessage(), e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi lấy thông tin sản phẩm: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Result<List<ProductManagementResponse>> getAllProducts() {
        try {
            List<Product> products = productRepository.findAll();
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
    public Result<List<ProductManagementResponse>> getProductsByBusinessId(Long businessId) {
        try {
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
    public Result<Void> deleteProduct(Long productId) {
        try {
            Product product = productRepository.findById(productId)
                    .orElse(null);

            if (product == null) {
                return Result.error(ErrorCode.NOT_FOUND, "Không tìm thấy sản phẩm với id: " + productId);
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

