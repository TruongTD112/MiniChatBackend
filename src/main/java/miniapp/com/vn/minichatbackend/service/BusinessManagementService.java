package miniapp.com.vn.minichatbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniapp.com.vn.minichatbackend.common.ErrorCode;
import miniapp.com.vn.minichatbackend.common.Result;
import miniapp.com.vn.minichatbackend.dto.request.CreateBusinessRequest;
import miniapp.com.vn.minichatbackend.dto.request.UpdateBusinessRequest;
import miniapp.com.vn.minichatbackend.dto.response.BusinessManagementResponse;
import miniapp.com.vn.minichatbackend.entity.BackOfficeBusiness;
import miniapp.com.vn.minichatbackend.entity.Business;
import miniapp.com.vn.minichatbackend.common.Constants;
import miniapp.com.vn.minichatbackend.repo.BackOfficeBusinessRepository;
import miniapp.com.vn.minichatbackend.repo.BusinessRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessManagementService {

    private final BusinessRepository businessRepository;
    private final BackOfficeBusinessRepository backOfficeBusinessRepository;

    /**
     * Kiểm tra user hiện tại có quyền truy cập business (có trong bảng BackOffice_Business) không.
     * @return null nếu có quyền, otherwise ErrorCode và message để trả về lỗi
     */
    private AccessCheck failureIfNoAccess(Long backOfficeUserId, Long businessId) {
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
    public Result<BusinessManagementResponse> createBusiness(CreateBusinessRequest request, Long backOfficeUserId) {
        try {
            if (backOfficeUserId == null) {
                return Result.error(ErrorCode.UNAUTHORIZED, "Unauthorized");
            }
            Business business = Business.builder()
                    .name(request.getName())
                    .phone(request.getPhone())
                    .address(request.getAddress())
                    .description(request.getDescription())
                    .status(request.getStatus() != null ? request.getStatus() : 1)
                    .metadata(request.getMetadata())
                    .style(request.getStyle())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Business savedBusiness = businessRepository.save(business);

            // Gán business cho user trong bảng BackOffice_Business (owner)
            BackOfficeBusiness backOfficeBusiness = BackOfficeBusiness.builder()
                    .businessId(savedBusiness.getId())
                    .backOfficeUserId(backOfficeUserId)
                    .status(Constants.BackOfficeConstants.ACTIVE)
                    .role(Constants.BackOfficeBusinessRole.OWNER)
                    .createdAt(LocalDateTime.now())
                    .build();
            backOfficeBusinessRepository.save(backOfficeBusiness);

            log.info("Business created successfully: id={}, name={}, backOfficeUserId={}", savedBusiness.getId(), savedBusiness.getName(), backOfficeUserId);
            return Result.success(toResponse(savedBusiness));
        } catch (Exception e) {
            log.error("Error creating business: {}", e.getMessage(), e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi tạo business: " + e.getMessage());
        }
    }

    @Transactional
    public Result<BusinessManagementResponse> updateBusiness(Long businessId, UpdateBusinessRequest request, Long backOfficeUserId) {
        try {
            AccessCheck noAccess = failureIfNoAccess(backOfficeUserId, businessId);
            if (noAccess != null) {
                return Result.error(noAccess.errorCode(), noAccess.message());
            }
            Business business = businessRepository.findById(businessId)
                    .orElse(null);

            if (business == null) {
                return Result.error(ErrorCode.NOT_FOUND, "Không tìm thấy business với id: " + businessId);
            }

            if (request.getName() != null) {
                business.setName(request.getName());
            }
            if (request.getPhone() != null) {
                business.setPhone(request.getPhone());
            }
            if (request.getAddress() != null) {
                business.setAddress(request.getAddress());
            }
            if (request.getDescription() != null) {
                business.setDescription(request.getDescription());
            }
            if (request.getStatus() != null) {
                business.setStatus(request.getStatus());
            }
            if (request.getMetadata() != null) {
                business.setMetadata(request.getMetadata());
            }
            if (request.getStyle() != null) {
                business.setStyle(request.getStyle());
            }
            business.setUpdatedAt(LocalDateTime.now());

            Business updatedBusiness = businessRepository.save(business);
            log.info("Business updated successfully: id={}, name={}", updatedBusiness.getId(), updatedBusiness.getName());

            return Result.success(toResponse(updatedBusiness));
        } catch (Exception e) {
            log.error("Error updating business: {}", e.getMessage(), e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi cập nhật business: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Result<BusinessManagementResponse> getBusinessById(Long businessId, Long backOfficeUserId) {
        try {
            AccessCheck noAccess = failureIfNoAccess(backOfficeUserId, businessId);
            if (noAccess != null) {
                return Result.error(noAccess.errorCode(), noAccess.message());
            }
            Business business = businessRepository.findById(businessId)
                    .orElse(null);

            if (business == null) {
                return Result.error(ErrorCode.NOT_FOUND, "Không tìm thấy business với id: " + businessId);
            }

            return Result.success(toResponse(business));
        } catch (Exception e) {
            log.error("Error getting business by id: {}", e.getMessage(), e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi lấy thông tin business: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Result<List<BusinessManagementResponse>> getAllBusinesses(Long backOfficeUserId) {
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
            List<Business> businesses = businessRepository.findAllById(businessIds);
            List<BusinessManagementResponse> responses = businesses.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());

            return Result.success(responses);
        } catch (Exception e) {
            log.error("Error getting all businesses: {}", e.getMessage(), e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi lấy danh sách business: " + e.getMessage());
        }
    }

    @Transactional
    public Result<Void> deleteBusiness(Long businessId, Long backOfficeUserId) {
        try {
            AccessCheck noAccess = failureIfNoAccess(backOfficeUserId, businessId);
            if (noAccess != null) {
                return Result.error(noAccess.errorCode(), noAccess.message());
            }
            Business business = businessRepository.findById(businessId)
                    .orElse(null);

            if (business == null) {
                return Result.error(ErrorCode.NOT_FOUND, "Không tìm thấy business với id: " + businessId);
            }

            // Xóa tất cả quan hệ BackOffice_Business của business này (owner + staff)
            List<BackOfficeBusiness> links = backOfficeBusinessRepository.findByBusinessId(businessId);
            backOfficeBusinessRepository.deleteAll(links);
            businessRepository.delete(business);
            log.info("Business deleted successfully: id={}", businessId);

            return Result.success(null);
        } catch (Exception e) {
            log.error("Error deleting business: {}", e.getMessage(), e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi xóa business: " + e.getMessage());
        }
    }

    private BusinessManagementResponse toResponse(Business business) {
        return BusinessManagementResponse.builder()
                .id(business.getId())
                .name(business.getName())
                .phone(business.getPhone())
                .address(business.getAddress())
                .description(business.getDescription())
                .status(business.getStatus())
                .metadata(business.getMetadata())
                .style(business.getStyle())
                .build();
    }
}

