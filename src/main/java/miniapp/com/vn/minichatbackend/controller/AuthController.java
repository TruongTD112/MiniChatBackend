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
import miniapp.com.vn.minichatbackend.common.Result;
import miniapp.com.vn.minichatbackend.dto.request.GetChannelsRequest;
import miniapp.com.vn.minichatbackend.dto.request.SocialLoginRequest;
import miniapp.com.vn.minichatbackend.dto.response.ChannelListResponse;
import miniapp.com.vn.minichatbackend.dto.response.SocialLoginResponse;
import miniapp.com.vn.minichatbackend.service.ChannelService;
import miniapp.com.vn.minichatbackend.service.SocialAuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "API xác thực")
public class AuthController {
	private final SocialAuthService socialAuthService;
	private final ChannelService channelService;

	@PostMapping("/social")
	@Operation(
		summary = "Đăng nhập bằng Social (Google/Facebook)",
		description = "Xác thực user bằng Google ID token hoặc Facebook access token và trả về JWT token"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Đăng nhập thành công",
			content = @Content(schema = @Schema(implementation = SocialLoginResponse.class))),
		@ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
		@ApiResponse(responseCode = "401", description = "Xác thực thất bại - Token không hợp lệ"),
		@ApiResponse(responseCode = "500", description = "Lỗi server")
	})
	public Result<SocialLoginResponse> socialLogin(
		@Parameter(description = "Thông tin đăng nhập (provider: 'google' hoặc 'facebook', token: ID token hoặc access token)", required = true)
		@RequestBody SocialLoginRequest request) {
		Result<SocialLoginResponse> result = socialAuthService.login(request);
		if (result.isSuccess()) {
			log.info("Social login successful: userId={}, provider={}",
				result.getData().getUserId(), result.getData().getProvider());
		} else {
			log.warn("Social login failed: code={}, message={}", result.getCode(), result.getMessage());
		}
		return result;
	}
}
