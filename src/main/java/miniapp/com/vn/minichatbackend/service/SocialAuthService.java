package miniapp.com.vn.minichatbackend.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniapp.com.vn.minichatbackend.common.ErrorCode;
import miniapp.com.vn.minichatbackend.common.Result;
import miniapp.com.vn.minichatbackend.dto.request.SocialLoginRequest;
import miniapp.com.vn.minichatbackend.dto.response.SocialLoginResponse;
import miniapp.com.vn.minichatbackend.entity.CustomerUser;
import miniapp.com.vn.minichatbackend.repo.CustomerUserRepository;
import miniapp.com.vn.minichatbackend.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialAuthService {
	private final CustomerUserRepository customerUserRepository;
	private final JwtService jwtService;
	private final GoogleTokenVerificationService googleTokenVerificationService;

	@Transactional
	public Result<SocialLoginResponse> login(SocialLoginRequest request) {
		if (request == null || request.getProvider() == null || request.getToken() == null) {
			log.warn("Invalid social login request: request={}, provider={}, token={}",
				request != null, request != null ? request.getProvider() : "null", request != null ? request.getToken() : "null");
			return Result.error(ErrorCode.INVALID_REQUEST, "Invalid request");
		}
		String provider = request.getProvider().trim().toLowerCase();
		String token = request.getToken().trim();

		// Verify token based on provider
		GoogleTokenVerificationService.GoogleUserInfo googleUserInfo = null;
		String providerId;
		String email;
		String name;
		String avatar;

		if ("google".equals(provider)) {
			// Verify Google ID token
			googleUserInfo = googleTokenVerificationService.verifyAndGetUserInfo(token);
			if (googleUserInfo == null) {
				log.warn("Google token verification failed for token: {}", token);
				return Result.error(ErrorCode.AUTHENTICATION_FAILED, "Google token verification failed");
			}
			
			// Extract user info from Google
			providerId = googleUserInfo.getSub();
			email = googleUserInfo.getEmail();
			name = googleUserInfo.getName();
			avatar = googleUserInfo.getPicture();
			
			// Verify email is verified
			if (googleUserInfo.getEmailVerified() == null || !googleUserInfo.getEmailVerified()) {
				log.warn("Google email not verified for user: {}", email);
				return Result.error(ErrorCode.AUTHENTICATION_FAILED, "Google email not verified");
			}
			
			log.info("Google token verified successfully for user: {}", email);
		} else {
			// Fallback for other providers (Facebook, etc.)
			// TODO: Add Facebook verification when needed
			log.warn("Provider {} not supported for token verification, using fallback", provider);
			providerId = token;
			email = provider + "+" + providerId + "@example.com";
			name = provider.substring(0, 1).toUpperCase() + provider.substring(1) + " User";
			avatar = null;
		}

		Optional<CustomerUser> existing = customerUserRepository.findByProviderId(providerId);
		CustomerUser user = existing.orElseGet(() -> CustomerUser.builder()
			.name(name)
			.email(email)
			.provider(provider)
			.providerId(providerId)
			.profilePictureUrl(avatar)
			.build());

		// Update profile if needed
		user.setName(name);
		user.setEmail(email);
		user.setProvider(provider);
		user.setProviderId(providerId);
		user.setProfilePictureUrl(avatar);

		CustomerUser saved = customerUserRepository.save(user);

		Map<String, Object> claims = new HashMap<>();
		claims.put("uid", saved.getId());
		claims.put("email", saved.getEmail());
		claims.put("provider", saved.getProvider());
		String jwt = jwtService.generateToken("user:" + saved.getId(), claims);

		SocialLoginResponse response = SocialLoginResponse.builder()
			.token(jwt)
			.userId(saved.getId())
			.name(saved.getName())
			.email(saved.getEmail())
			.provider(saved.getProvider())
			.providerId(saved.getProviderId())
			.profilePictureUrl(saved.getProfilePictureUrl())
			.build();

		log.info("Social login successful: userId={}, provider={}, isNewUser={}",
			saved.getId(), provider, !existing.isPresent());
		return Result.success(response);
	}
}
