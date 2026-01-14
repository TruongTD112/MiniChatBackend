package miniapp.com.vn.minichatbackend.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniapp.com.vn.minichatbackend.common.Result;
import miniapp.com.vn.minichatbackend.dto.request.SocialLoginRequest;
import miniapp.com.vn.minichatbackend.dto.response.SocialLoginResponse;
import miniapp.com.vn.minichatbackend.service.SocialAuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
	private final SocialAuthService socialAuthService;

	@PostMapping("/social")
	public Result<SocialLoginResponse> socialLogin(@RequestBody SocialLoginRequest request) {
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
