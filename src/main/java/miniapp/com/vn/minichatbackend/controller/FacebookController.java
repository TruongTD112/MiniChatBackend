package miniapp.com.vn.minichatbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniapp.com.vn.minichatbackend.common.ErrorCode;
import miniapp.com.vn.minichatbackend.common.Result;
import miniapp.com.vn.minichatbackend.dto.request.ConnectFacebookPageRequest;
import miniapp.com.vn.minichatbackend.dto.request.FacebookPageRequest;
import miniapp.com.vn.minichatbackend.dto.request.GetChannelsRequest;
import miniapp.com.vn.minichatbackend.dto.request.GetConversationHistoryRequest;
import miniapp.com.vn.minichatbackend.dto.request.GetFacebookConversationsRequest;
import miniapp.com.vn.minichatbackend.dto.request.GetFacebookUserInfoRequest;
import miniapp.com.vn.minichatbackend.dto.request.SendMessageRequest;
import miniapp.com.vn.minichatbackend.dto.response.ChannelListResponse;
import miniapp.com.vn.minichatbackend.dto.response.ConversationHistoryResponse;
import miniapp.com.vn.minichatbackend.dto.response.FacebookUserInfoResponse;
import miniapp.com.vn.minichatbackend.dto.response.SendMessageResponse;
import miniapp.com.vn.minichatbackend.dto.response.ConnectFacebookPageResponse;
import miniapp.com.vn.minichatbackend.dto.response.FacebookConversationResponse;
import miniapp.com.vn.minichatbackend.dto.response.FacebookPageResponse;
import miniapp.com.vn.minichatbackend.entity.Channel;
import miniapp.com.vn.minichatbackend.service.ChannelService;
import miniapp.com.vn.minichatbackend.service.FacebookConversationService;
import miniapp.com.vn.minichatbackend.service.FacebookPageService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller quản lý các API liên quan đến Facebook
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/facebook")
@Tag(name = "Facebook", description = "API quản lý Facebook Pages và Conversations")
public class FacebookController {
	
	private final FacebookPageService facebookPageService;
	private final ChannelService channelService;
	private final FacebookConversationService facebookConversationService;

	private Long getCurrentUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.getPrincipal() != null) {
			if (auth.getPrincipal() instanceof Integer) {
				return ((Integer) auth.getPrincipal()).longValue();
			}
			if (auth.getPrincipal() instanceof Long) {
				return (Long) auth.getPrincipal();
			}
		}
		return null;
	}

	/**
	 * API lấy danh sách Channels/Pages đã connect thành công
	 * @param request Chứa businessId và type (platform)
	 * @return Danh sách channels đã connect
	 */
	@PostMapping("/channels")
	@Operation(
			summary = "Lấy danh sách Channels đã connect",
			description = "Lấy danh sách các Channels/Pages đã được connect vào Business. " +
					"Có thể lọc theo platform type (FACEBOOK, ZALO, etc.) hoặc lấy tất cả nếu không chỉ định type."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lấy danh sách Channels thành công",
					content = @Content(schema = @Schema(implementation = ChannelListResponse.class))),
			@ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ - businessId không được để trống"),
			@ApiResponse(responseCode = "404", description = "Business không tồn tại"),
			@ApiResponse(responseCode = "500", description = "Lỗi server")
	})
	public Result<ChannelListResponse> getChannels(
			@Parameter(description = "Thông tin lấy danh sách (businessId: ID của Business, type: Platform type như FACEBOOK - để trống để lấy tất cả)", required = true)
			@RequestBody GetChannelsRequest request) {
		if (request == null || request.getBusinessId() == null) {
			log.warn("Get channels request is null or missing businessId");
			return Result.error(miniapp.com.vn.minichatbackend.common.ErrorCode.INVALID_REQUEST,
					"businessId không được để trống");
		}

		Result<ChannelListResponse> result = channelService.getChannels(request);
		if (result.isSuccess()) {
			log.info("Successfully retrieved {} channels for businessId={}, type={}",
					result.getData() != null && result.getData().getChannels() != null
							? result.getData().getChannels().size() : 0,
					request.getBusinessId(),
					request.getType() != null ? request.getType() : "ALL");
		} else {
			log.warn("Failed to retrieve channels: code={}, message={}",
					result.getCode(), result.getMessage());
		}
		return result;
	}

	/**
	 * API lấy danh sách Facebook Pages từ user token
	 * @param request Chứa Facebook user access token
	 * @return Danh sách pages với id, name, avatar
	 */
	@PostMapping("/pages")
	@Operation(
		summary = "Lấy danh sách Facebook Pages",
		description = "Lấy danh sách các Facebook Pages mà user có quyền quản lý. " +
			"User token cần có quyền 'pages_show_list' hoặc 'pages_read_engagement'. " +
			"API sẽ gọi Facebook Graph API để lấy thông tin pages bao gồm: Page ID, tên Page và avatar."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Lấy danh sách Pages thành công",
			content = @Content(schema = @Schema(implementation = FacebookPageResponse.class))),
		@ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ - User token không được để trống"),
		@ApiResponse(responseCode = "401", description = "Facebook token không hợp lệ hoặc đã hết hạn"),
		@ApiResponse(responseCode = "500", description = "Lỗi server - Không thể kết nối đến Facebook API")
	})
	public Result<FacebookPageResponse> getFacebookPages(
		@Parameter(description = "Facebook user access token từ client sau khi đăng nhập Facebook", required = true)
		@RequestBody FacebookPageRequest request) {
		if (request == null || request.getUserToken() == null) {
			log.warn("Facebook page request is null or missing userToken");
			return Result.error(miniapp.com.vn.minichatbackend.common.ErrorCode.INVALID_REQUEST, 
				"Facebook user token không được để trống");
		}
		
		Result<FacebookPageResponse> result = facebookPageService.getPages(request.getUserToken());
		if (result.isSuccess()) {
			log.info("Successfully retrieved {} Facebook pages", 
				result.getData() != null && result.getData().getPages() != null 
					? result.getData().getPages().size() : 0);
		} else {
			log.warn("Failed to retrieve Facebook pages: code={}, message={}", 
				result.getCode(), result.getMessage());
		}
		return result;
	}

	/**
	 * API connect Facebook Page vào ứng dụng
	 * @param request Chứa userToken, pageId và businessId
	 * @return Thông tin Channel đã tạo
	 */
	@PostMapping("/connect")
	@Operation(
		summary = "Connect Facebook Page vào ứng dụng",
		description = "Connect một Facebook Page vào Business trong ứng dụng. " +
			"API sẽ gọi Facebook Graph API để lấy thông tin Page và Page Access Token, " +
			"sau đó mã hóa và lưu vào bảng Channel. " +
			"Page Access Token sẽ được mã hóa bằng AES trước khi lưu vào database."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Connect Page thành công",
			content = @Content(schema = @Schema(implementation = ConnectFacebookPageResponse.class))),
		@ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ - Thiếu thông tin bắt buộc"),
		@ApiResponse(responseCode = "401", description = "Facebook token không hợp lệ hoặc đã hết hạn"),
		@ApiResponse(responseCode = "404", description = "Không tìm thấy Business hoặc Page"),
		@ApiResponse(responseCode = "409", description = "Channel đã được connect vào Business này rồi"),
		@ApiResponse(responseCode = "500", description = "Lỗi server - Không thể kết nối đến Facebook API hoặc lỗi mã hóa")
	})
	public Result<ConnectFacebookPageResponse> connectFacebookPage(
		@Parameter(description = "Thông tin connect Page (userToken: Facebook user token, pageId: Page ID cần connect, businessId: ID của Business)", required = true)
		@RequestBody ConnectFacebookPageRequest request) {
		if (request == null || request.getUserToken() == null || request.getPageId() == null || request.getBusinessId() == null) {
			log.warn("Connect Facebook page request is null or missing required fields");
			return Result.error(miniapp.com.vn.minichatbackend.common.ErrorCode.INVALID_REQUEST, 
				"userToken, pageId và businessId không được để trống");
		}
		
		Result<ConnectFacebookPageResponse> result = channelService.connectFacebookPage(request);
		if (result.isSuccess()) {
			log.info("Successfully connected Facebook Page: channelId={}, pageId={}, businessId={}", 
				result.getData().getId(), request.getPageId(), request.getBusinessId());
		} else {
			log.warn("Failed to connect Facebook Page: code={}, message={}", 
				result.getCode(), result.getMessage());
		}
		return result;
	}

	/**
	 * API ngắt kết nối (disconnect) Facebook Page khỏi Business.
	 * Chỉ user có quyền với business (BackOffice_Business) mới được disconnect.
	 */
	@DeleteMapping("/channel/{channelId}")
	@Operation(
		summary = "Disconnect Facebook Page",
		description = "Ngắt kết nối Facebook Page khỏi Business. Xóa channel và token đã lưu. Chỉ user có quyền với business mới thực hiện được."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Ngắt kết nối thành công"),
		@ApiResponse(responseCode = "401", description = "Chưa xác thực"),
		@ApiResponse(responseCode = "403", description = "Không có quyền ngắt kết nối channel này"),
		@ApiResponse(responseCode = "404", description = "Channel không tồn tại"),
		@ApiResponse(responseCode = "500", description = "Lỗi server")
	})
	public Result<Void> disconnectChannel(
		@Parameter(description = "ID của Channel (Facebook Page) cần ngắt kết nối", required = true, example = "1")
		@PathVariable Long channelId) {
		Long userId = getCurrentUserId();
		if (userId == null) {
			return Result.error(ErrorCode.INVALID_CREDENTIALS, "Unauthorized");
		}
		log.info("Disconnect channel request: userId={}, channelId={}", userId, channelId);
		return channelService.disconnectChannel(channelId, userId);
	}

	/**
	 * API lấy danh sách Conversations từ Facebook Page
	 * @param request Chứa channelId và limit
	 * @return Danh sách conversations từ Facebook
	 */
	@PostMapping("/conversations")
	@Operation(
		summary = "Lấy danh sách Conversations từ Facebook Page",
		description = "Lấy danh sách các Conversations từ Facebook Page đã được connect. " +
			"API sẽ lấy Page Access Token từ Channel (đã giải mã), sau đó gọi Facebook Graph API " +
			"để lấy danh sách conversations bao gồm: participants, sender, last message, và thông tin pagination."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Lấy danh sách Conversations thành công",
			content = @Content(schema = @Schema(implementation = FacebookConversationResponse.class))),
		@ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ - channelId không được để trống"),
		@ApiResponse(responseCode = "404", description = "Channel không tồn tại hoặc không có Page Access Token"),
		@ApiResponse(responseCode = "401", description = "Page Access Token không hợp lệ hoặc đã hết hạn"),
		@ApiResponse(responseCode = "500", description = "Lỗi server - Không thể kết nối đến Facebook API")
	})
	public Result<FacebookConversationResponse> getFacebookConversations(
		@Parameter(description = "Thông tin lấy conversations (channelId: ID của Channel đã connect, limit: Số lượng conversations - mặc định 25)", required = true)
		@RequestBody GetFacebookConversationsRequest request) {
		if (request == null || request.getChannelId() == null) {
			log.warn("Get Facebook conversations request is null or missing channelId");
			return Result.error(miniapp.com.vn.minichatbackend.common.ErrorCode.INVALID_REQUEST, 
				"channelId không được để trống");
		}
		
		// Lấy Channel từ database
		Channel channel = channelService.getChannelById(request.getChannelId());
		if (channel == null) {
			log.warn("Channel not found: channelId={}", request.getChannelId());
			return Result.error(miniapp.com.vn.minichatbackend.common.ErrorCode.NOT_FOUND, 
				"Channel không tồn tại");
		}
		
		// Kiểm tra platform phải là FACEBOOK
		if (!"FACEBOOK".equalsIgnoreCase(channel.getPlatform())) {
			log.warn("Channel is not Facebook platform: channelId={}, platform={}", 
				request.getChannelId(), channel.getPlatform());
			return Result.error(miniapp.com.vn.minichatbackend.common.ErrorCode.INVALID_REQUEST, 
				"Channel này không phải là Facebook Page");
		}
		
		// Lấy Page Access Token (đã giải mã)
		String pageAccessToken = channelService.getPageAccessToken(request.getChannelId());
		if (pageAccessToken == null) {
			log.warn("Cannot get page access token: channelId={}", request.getChannelId());
			return Result.error(miniapp.com.vn.minichatbackend.common.ErrorCode.NOT_FOUND, 
				"Không thể lấy Page Access Token từ Channel");
		}
		
		// Gọi Facebook API để lấy conversations
		Result<FacebookConversationResponse> result = facebookConversationService.getConversations(
			channel.getChannelId(), // Page ID từ Facebook
			pageAccessToken,
			request.getLimit()
		);
		
		if (result.isSuccess()) {
			log.info("Successfully retrieved {} conversations for channelId={}", 
				result.getData() != null && result.getData().getConversations() != null 
					? result.getData().getConversations().size() : 0,
				request.getChannelId());
		} else {
			log.warn("Failed to retrieve Facebook conversations: code={}, message={}", 
				result.getCode(), result.getMessage());
		}
		return result;
	}

	/**
	 * API lấy lịch sử tin nhắn (messages) của một Conversation từ Facebook.
	 * Mặc định lấy 25 tin nhắn mới nhất nếu không truyền limit.
	 *
	 * @param request Chứa channelId, conversationId (Facebook), limit (optional, mặc định 25), after (cursor pagination)
	 * @return Danh sách messages trong conversation
	 */
	@PostMapping("/conversations/history")
	@Operation(
		summary = "Lấy lịch sử tin nhắn theo Conversation",
		description = "Lấy lịch sử tin nhắn (messages) của một Conversation từ Facebook. " +
			"Mặc định lấy 25 tin nhắn mới nhất. Cần truyền conversationId (id từ API conversations) và channelId."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Lấy lịch sử tin nhắn thành công",
			content = @Content(schema = @Schema(implementation = ConversationHistoryResponse.class))),
		@ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ - channelId hoặc conversationId trống"),
		@ApiResponse(responseCode = "404", description = "Channel không tồn tại hoặc không có Page Access Token"),
		@ApiResponse(responseCode = "401", description = "Page Access Token không hợp lệ hoặc đã hết hạn"),
		@ApiResponse(responseCode = "500", description = "Lỗi server - Không thể kết nối đến Facebook API")
	})
	public Result<ConversationHistoryResponse> getConversationHistory(
		@Parameter(description = "channelId: ID Channel đã connect; conversationId: Conversation ID từ Facebook (vd: t_123); limit: số tin (mặc định 25); after: cursor trang sau", required = true)
		@RequestBody GetConversationHistoryRequest request) {
		if (request == null || request.getChannelId() == null) {
			log.warn("Get conversation history request is null or missing channelId");
			return Result.error(miniapp.com.vn.minichatbackend.common.ErrorCode.INVALID_REQUEST,
				"channelId không được để trống");
		}
		if (request.getConversationId() == null || request.getConversationId().trim().isEmpty()) {
			log.warn("Get conversation history request missing conversationId");
			return Result.error(miniapp.com.vn.minichatbackend.common.ErrorCode.INVALID_REQUEST,
				"conversationId không được để trống");
		}

		Channel channel = channelService.getChannelById(request.getChannelId());
		if (channel == null) {
			log.warn("Channel not found: channelId={}", request.getChannelId());
			return Result.error(miniapp.com.vn.minichatbackend.common.ErrorCode.NOT_FOUND,
				"Channel không tồn tại");
		}
		if (!"FACEBOOK".equalsIgnoreCase(channel.getPlatform())) {
			log.warn("Channel is not Facebook: channelId={}, platform={}", request.getChannelId(), channel.getPlatform());
			return Result.error(miniapp.com.vn.minichatbackend.common.ErrorCode.INVALID_REQUEST,
				"Channel này không phải là Facebook Page");
		}

		String pageAccessToken = channelService.getPageAccessToken(request.getChannelId());
		if (pageAccessToken == null) {
			log.warn("Cannot get page access token: channelId={}", request.getChannelId());
			return Result.error(miniapp.com.vn.minichatbackend.common.ErrorCode.NOT_FOUND,
				"Không thể lấy Page Access Token từ Channel");
		}

		Integer limit = (request.getLimit() != null && request.getLimit() > 0) ? request.getLimit() : 25;
		Result<ConversationHistoryResponse> result = facebookConversationService.getConversationMessages(
			request.getConversationId().trim(),
			pageAccessToken,
			limit,
			request.getAfter()
		);

		if (result.isSuccess()) {
			log.info("Successfully retrieved {} messages for conversationId={}, channelId={}",
				result.getData() != null && result.getData().getMessages() != null
					? result.getData().getMessages().size() : 0,
				request.getConversationId(), request.getChannelId());
		} else {
			log.warn("Failed to get conversation history: code={}, message={}",
				result.getCode(), result.getMessage());
		}
		return result;
	}

	/**
	 * API gửi tin nhắn từ Fanpage đến người dùng (chat với fanpage).
	 * Dùng Facebook Send API, recipientId là PSID (lấy từ conversation participants/sender).
	 */
	@PostMapping("/messages/send")
	@Operation(
		summary = "Gửi tin nhắn từ Fanpage đến người dùng",
		description = "Gửi tin nhắn text hoặc ảnh từ Fanpage đến người dùng qua Facebook Messenger. " +
			"recipientId là Page-Scoped User ID (PSID), lấy từ conversation participants hoặc sender. " +
			"Nếu gửi cả text và imageUrl, text sẽ được gửi trước."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Gửi tin nhắn thành công",
			content = @Content(schema = @Schema(implementation = SendMessageResponse.class))),
		@ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ - thiếu channelId, recipientId, text hoặc imageUrl"),
		@ApiResponse(responseCode = "404", description = "Channel không tồn tại hoặc không có Page Access Token"),
		@ApiResponse(responseCode = "401", description = "Page Access Token không hợp lệ hoặc đã hết hạn"),
		@ApiResponse(responseCode = "500", description = "Lỗi server - Không thể gửi tin nhắn qua Facebook API")
	})
	public Result<SendMessageResponse> sendMessage(
		@Parameter(description = "channelId: ID Channel (Fanpage); recipientId: PSID người nhận; text: nội dung tin nhắn; imageUrl: link ảnh", required = true)
		@RequestBody SendMessageRequest request) {
		if (request == null || request.getChannelId() == null) {
			log.warn("Send message request is null or missing channelId");
			return Result.error(miniapp.com.vn.minichatbackend.common.ErrorCode.INVALID_REQUEST,
				"channelId không được để trống");
		}
		if (request.getRecipientId() == null || request.getRecipientId().trim().isEmpty()) {
			log.warn("Send message request missing recipientId");
			return Result.error(miniapp.com.vn.minichatbackend.common.ErrorCode.INVALID_REQUEST,
				"recipientId (PSID) không được để trống");
		}
		
		boolean hasText = request.getText() != null && !request.getText().trim().isEmpty();
		boolean hasImage = request.getImageUrl() != null && !request.getImageUrl().trim().isEmpty();

		if (!hasText && !hasImage) {
			log.warn("Send message request missing text and imageUrl");
			return Result.error(miniapp.com.vn.minichatbackend.common.ErrorCode.INVALID_REQUEST,
				"text hoặc imageUrl không được để trống");
		}

		Channel channel = channelService.getChannelById(request.getChannelId());
		if (channel == null) {
			log.warn("Channel not found: channelId={}", request.getChannelId());
			return Result.error(miniapp.com.vn.minichatbackend.common.ErrorCode.NOT_FOUND,
				"Channel không tồn tại");
		}
		if (!"FACEBOOK".equalsIgnoreCase(channel.getPlatform())) {
			log.warn("Channel is not Facebook: channelId={}, platform={}", request.getChannelId(), channel.getPlatform());
			return Result.error(miniapp.com.vn.minichatbackend.common.ErrorCode.INVALID_REQUEST,
				"Channel này không phải là Facebook Page");
		}

		String pageAccessToken = channelService.getPageAccessToken(request.getChannelId());
		if (pageAccessToken == null) {
			log.warn("Cannot get page access token: channelId={}", request.getChannelId());
			return Result.error(miniapp.com.vn.minichatbackend.common.ErrorCode.NOT_FOUND,
				"Không thể lấy Page Access Token từ Channel");
		}

		Result<SendMessageResponse> result = facebookConversationService.sendMessage(
			pageAccessToken,
			request.getRecipientId().trim(),
			request.getText() != null ? request.getText().trim() : null,
			request.getImageUrl() != null ? request.getImageUrl().trim() : null
		);
		if (result.isSuccess()) {
			log.info("Successfully sent message to recipientId={}, channelId={}",
				request.getRecipientId(), request.getChannelId());
		} else {
			log.warn("Failed to send message: code={}, message={}", result.getCode(), result.getMessage());
		}
		return result;
	}

	/**
	 * API lấy thông tin người dùng (tên, avatar) từ Facebook theo PSID.
	 */
	@PostMapping("/users/info")
	@Operation(
		summary = "Lấy thông tin người dùng Facebook (tên, avatar)",
		description = "Lấy tên và avatar (profile picture) của người dùng theo Page-Scoped User ID (PSID). " +
			"PSID lấy từ conversation participants hoặc sender."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Lấy thông tin người dùng thành công",
			content = @Content(schema = @Schema(implementation = FacebookUserInfoResponse.class))),
		@ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ - thiếu channelId hoặc psid"),
		@ApiResponse(responseCode = "404", description = "Channel không tồn tại hoặc không có Page Access Token"),
		@ApiResponse(responseCode = "401", description = "Page Access Token không hợp lệ hoặc đã hết hạn"),
		@ApiResponse(responseCode = "500", description = "Lỗi server - Không thể lấy thông tin từ Facebook API")
	})
	public Result<FacebookUserInfoResponse> getUserInfo(
		@Parameter(description = "channelId: ID Channel (Fanpage); psid: Page-Scoped User ID cần lấy thông tin", required = true)
		@RequestBody GetFacebookUserInfoRequest request) {
		if (request == null || request.getChannelId() == null) {
			log.warn("Get user info request is null or missing channelId");
			return Result.error(miniapp.com.vn.minichatbackend.common.ErrorCode.INVALID_REQUEST,
				"channelId không được để trống");
		}
		if (request.getPsid() == null || request.getPsid().trim().isEmpty()) {
			log.warn("Get user info request missing psid");
			return Result.error(miniapp.com.vn.minichatbackend.common.ErrorCode.INVALID_REQUEST,
				"psid không được để trống");
		}

		Channel channel = channelService.getChannelById(request.getChannelId());
		if (channel == null) {
			log.warn("Channel not found: channelId={}", request.getChannelId());
			return Result.error(miniapp.com.vn.minichatbackend.common.ErrorCode.NOT_FOUND,
				"Channel không tồn tại");
		}
		if (!"FACEBOOK".equalsIgnoreCase(channel.getPlatform())) {
			log.warn("Channel is not Facebook: channelId={}, platform={}", request.getChannelId(), channel.getPlatform());
			return Result.error(miniapp.com.vn.minichatbackend.common.ErrorCode.INVALID_REQUEST,
				"Channel này không phải là Facebook Page");
		}

		String pageAccessToken = channelService.getPageAccessToken(request.getChannelId());
		if (pageAccessToken == null) {
			log.warn("Cannot get page access token: channelId={}", request.getChannelId());
			return Result.error(miniapp.com.vn.minichatbackend.common.ErrorCode.NOT_FOUND,
				"Không thể lấy Page Access Token từ Channel");
		}

		Result<FacebookUserInfoResponse> result = facebookConversationService.getUserProfile(
			pageAccessToken,
			request.getPsid().trim()
		);
		if (result.isSuccess()) {
			log.info("Successfully retrieved user info for psid={}, channelId={}",
				request.getPsid(), request.getChannelId());
		} else {
			log.warn("Failed to get user info: code={}, message={}", result.getCode(), result.getMessage());
		}
		return result;
	}
}
