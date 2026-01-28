package miniapp.com.vn.minichatbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniapp.com.vn.minichatbackend.common.ErrorCode;
import miniapp.com.vn.minichatbackend.common.Result;
import miniapp.com.vn.minichatbackend.dto.request.ConnectFacebookPageRequest;
import miniapp.com.vn.minichatbackend.dto.request.GetChannelsRequest;
import miniapp.com.vn.minichatbackend.dto.response.ChannelListResponse;
import miniapp.com.vn.minichatbackend.dto.response.ConnectFacebookPageResponse;
import miniapp.com.vn.minichatbackend.entity.Channel;
import miniapp.com.vn.minichatbackend.repo.BusinessRepository;
import miniapp.com.vn.minichatbackend.repo.ChannelRepository;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service để quản lý Channel (Facebook Pages, Zalo, etc.)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelService {
    
    private static final String PLATFORM_FACEBOOK = "FACEBOOK";
    private static final Integer STATUS_ACTIVE = 1;
    
    private final ChannelRepository channelRepository;
    private final BusinessRepository businessRepository;
    private final FacebookPageService facebookPageService;
    private final EncryptionService encryptionService;
    
    /**
     * Connect Facebook Page vào ứng dụng
     * @param request Chứa userToken, pageId và businessId
     * @return Result chứa thông tin Channel đã tạo
     */
    @Transactional
    public Result<ConnectFacebookPageResponse> connectFacebookPage(ConnectFacebookPageRequest request) {
        // Validate request
        if (request == null || request.getUserToken() == null || request.getPageId() == null || request.getBusinessId() == null) {
            log.warn("Invalid connect Facebook page request");
            return Result.error(ErrorCode.INVALID_REQUEST, 
                "userToken, pageId và businessId không được để trống");
        }
        
        // Kiểm tra Business có tồn tại không
        if (!businessRepository.existsById(request.getBusinessId())) {
            log.warn("Business not found: businessId={}", request.getBusinessId());
            return Result.error(ErrorCode.NOT_FOUND, "Business không tồn tại");
        }
        
        // Kiểm tra Channel đã tồn tại chưa (theo channelId và businessId)
        // Lưu ý: Có thể có nhiều channels với cùng channelId nhưng khác businessId
        boolean channelExists = channelRepository.findAll().stream()
            .anyMatch(ch -> request.getPageId().equals(ch.getChannelId()) 
                && request.getBusinessId().equals(ch.getBusinessId()));
        
        if (channelExists) {
            log.warn("Channel already exists: channelId={}, businessId={}", 
                request.getPageId(), request.getBusinessId());
            return Result.error(ErrorCode.CONFLICT, 
                "Channel đã được connect vào Business");
        }
        
        // Gọi Facebook API để lấy thông tin page và access token
        FacebookPageService.FacebookPageData pageData = 
            facebookPageService.getPageWithAccessToken(request.getUserToken(), request.getPageId());
        
        if (pageData == null) {
            log.warn("Page not found or cannot access: pageId={}", request.getPageId());
            return Result.error(ErrorCode.NOT_FOUND, 
                "Không tìm thấy Page hoặc không có quyền truy cập Page này");
        }
        
        if (pageData.getAccessToken() == null || pageData.getAccessToken().trim().isEmpty()) {
            log.warn("Page access token is null or empty: pageId={}", request.getPageId());
            return Result.error(ErrorCode.INTERNAL_ERROR, 
                "Không thể lấy Page Access Token từ Facebook");
        }
        
        // Lấy avatar URL
        String avatarUrl = null;
        if (pageData.getPicture() != null && 
            pageData.getPicture().getData() != null) {
            avatarUrl = pageData.getPicture().getData().getUrl();
        }
        
        // Mã hóa page access token trước khi lưu
        String encryptedAccessToken = encryptionService.encrypt(pageData.getAccessToken());
        
        // Tạo Channel mới
        Channel channel = Channel.builder()
            .channelId(request.getPageId())
            .name(pageData.getName())
            .avatarUrl(avatarUrl)
            .platform(PLATFORM_FACEBOOK)
            .businessId(request.getBusinessId())
            .status(STATUS_ACTIVE)
            .config(encryptedAccessToken) // Lưu encrypted token
            .settings(null) // Có thể lưu thêm settings sau
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        Channel savedChannel = channelRepository.save(channel);
        
        log.info("Successfully connected Facebook Page: channelId={}, pageId={}, businessId={}", 
            savedChannel.getId(), request.getPageId(), request.getBusinessId());
        
        // Tạo response
        ConnectFacebookPageResponse response = ConnectFacebookPageResponse.builder()
            .id(savedChannel.getId())
            .channelId(savedChannel.getChannelId())
            .name(savedChannel.getName())
            .avatarUrl(savedChannel.getAvatarUrl())
            .platform(savedChannel.getPlatform())
            .status(savedChannel.getStatus())
            .build();
        
        return Result.success(response);
    }
    
    /**
     * Lấy danh sách Channels đã connect theo businessId và platform type
     * @param request Chứa businessId và type (platform)
     * @return Result chứa danh sách channels
     */
    public Result<ChannelListResponse> getChannels(GetChannelsRequest request) {
        // Validate request
        if (request == null || request.getBusinessId() == null) {
            log.warn("Invalid get channels request");
            return Result.error(ErrorCode.INVALID_REQUEST, 
                "businessId không được để trống");
        }
        
        // Kiểm tra Business có tồn tại không
        if (!businessRepository.existsById(request.getBusinessId())) {
            log.warn("Business not found: businessId={}", request.getBusinessId());
            return Result.error(ErrorCode.NOT_FOUND, "Business không tồn tại");
        }
        
        // Lấy danh sách channels
        List<Channel> channels;
        if (request.getType() != null && !request.getType().trim().isEmpty()) {
            // Lấy theo platform cụ thể
            String platform = request.getType().trim().toUpperCase();
            channels = channelRepository.findByBusinessIdAndPlatform(
                request.getBusinessId(), platform);
            log.info("Getting channels by businessId={} and platform={}, found {} channels", 
                request.getBusinessId(), platform, channels.size());
        } else {
            // Lấy tất cả platforms
            channels = channelRepository.findByBusinessId(request.getBusinessId());
            log.info("Getting all channels by businessId={}, found {} channels", 
                request.getBusinessId(), channels.size());
        }
        
        // Chuyển đổi từ Entity sang DTO
        List<ChannelListResponse.ChannelInfo> channelInfos = channels.stream()
            .map(channel -> ChannelListResponse.ChannelInfo.builder()
                .id(channel.getId())
                .channelId(channel.getChannelId())
                .name(channel.getName())
                .avatarUrl(channel.getAvatarUrl())
                .platform(channel.getPlatform())
                .status(channel.getStatus())
                .createdAt(channel.getCreatedAt())
                .updatedAt(channel.getUpdatedAt())
                .build())
            .collect(Collectors.toList());
        
        ChannelListResponse response = ChannelListResponse.builder()
            .channels(channelInfos)
            .build();
        
        return Result.success(response);
    }
    
    /**
     * Lấy Page Access Token từ Channel (đã giải mã)
     * @param channelId Channel ID trong database
     * @return Page Access Token đã giải mã hoặc null nếu không tìm thấy
     */
    public String getPageAccessToken(Long channelId) {
        if (channelId == null) {
            log.warn("Channel ID is null");
            return null;
        }
        
        Optional<Channel> channelOpt = channelRepository.findById(channelId);
        if (channelOpt.isEmpty()) {
            log.warn("Channel not found: channelId={}", channelId);
            return null;
        }
        
        Channel channel = channelOpt.get();
        if (channel.getConfig() == null || channel.getConfig().trim().isEmpty()) {
            log.warn("Channel config (encrypted token) is null or empty: channelId={}", channelId);
            return null;
        }
        
        try {
            // Giải mã page access token
            String decryptedToken = encryptionService.decrypt(channel.getConfig());
            log.info("Successfully decrypted page access token for channelId={}", channelId);
            return decryptedToken;
        } catch (Exception e) {
            log.error("Error decrypting page access token for channelId={}", channelId, e);
            return null;
        }
    }
    
    /**
     * Lấy Channel theo ID
     * @param channelId Channel ID trong database
     * @return Channel hoặc null nếu không tìm thấy
     */
    public Channel getChannelById(Long channelId) {
        if (channelId == null) {
            return null;
        }
        return channelRepository.findById(channelId).orElse(null);
    }
}
