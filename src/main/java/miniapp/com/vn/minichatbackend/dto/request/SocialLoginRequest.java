package miniapp.com.vn.minichatbackend.dto.request;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SocialLoginRequest {
	String provider; // "google" or "facebook"
	String token;    // Google ID token or Facebook access token
}
