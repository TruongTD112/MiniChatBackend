package miniapp.com.vn.minichatbackend.dto.request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocialLoginRequest {
    private String provider; // "google" or "facebook"
    private String token;    // Google ID token or Facebook access token
}
