package miniapp.com.vn.minichatbackend.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SocialLoginResponse {
    private String token; // JWT
    private Long userId;
    private String name;
    private String email;
    private String provider;
    private String providerId;
    private String profilePictureUrl;
}
