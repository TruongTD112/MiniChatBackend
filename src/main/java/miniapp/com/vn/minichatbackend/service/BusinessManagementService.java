package miniapp.com.vn.minichatbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniapp.com.vn.minichatbackend.common.ErrorCode;
import miniapp.com.vn.minichatbackend.common.Result;
import miniapp.com.vn.minichatbackend.dto.request.CreateBusinessRequest;
import miniapp.com.vn.minichatbackend.dto.request.UpdateBusinessRequest;
import miniapp.com.vn.minichatbackend.dto.response.BusinessManagementResponse;
import miniapp.com.vn.minichatbackend.entity.Business;
import miniapp.com.vn.minichatbackend.repo.BusinessRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessManagementService {

    private final BusinessRepository businessRepository;

    @Transactional
    public Result<BusinessManagementResponse> createBusiness(CreateBusinessRequest request) {
        try {
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
            log.info("Business created successfully: id={}, name={}", savedBusiness.getId(), savedBusiness.getName());

            return Result.success(toResponse(savedBusiness));
        } catch (Exception e) {
            log.error("Error creating business: {}", e.getMessage(), e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi tạo business: " + e.getMessage());
        }
    }

    @Transactional
    public Result<BusinessManagementResponse> updateBusiness(Long businessId, UpdateBusinessRequest request) {
        try {
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
    public Result<BusinessManagementResponse> getBusinessById(Long businessId) {
        try {
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
    public Result<List<BusinessManagementResponse>> getAllBusinesses() {
        try {
            List<Business> businesses = businessRepository.findAll();
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
    public Result<Void> deleteBusiness(Long businessId) {
        try {
            Business business = businessRepository.findById(businessId)
                    .orElse(null);

            if (business == null) {
                return Result.error(ErrorCode.NOT_FOUND, "Không tìm thấy business với id: " + businessId);
            }

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

