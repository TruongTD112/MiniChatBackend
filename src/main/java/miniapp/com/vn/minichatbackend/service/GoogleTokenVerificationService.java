package miniapp.com.vn.minichatbackend.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

/**
 * Service để verify Google ID token
 */
@Slf4j
@Service
public class GoogleTokenVerificationService {
    
    private final GoogleIdTokenVerifier verifier;
    
    @Value("${google.oauth.client-id:}")
    private String clientId;
    
    public GoogleTokenVerificationService(@Value("${google.oauth.client-id:}") String clientId) {
        // Khởi tạo verifier với client ID
        this.clientId = clientId;
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
            .setAudience(Collections.singletonList(clientId))
            .build();
    }
    
    /**
     * Verify Google ID token và trả về thông tin user
     * @param idTokenString Google ID token string
     * @return GoogleIdToken nếu valid, null nếu invalid
     */
    public GoogleIdToken verifyToken(String idTokenString) {
        if (idTokenString == null || idTokenString.trim().isEmpty()) {
            log.warn("Google ID token is null or empty");
            return null;
        }
        
        try {
            // Verify token
            GoogleIdToken idToken = verifier.verify(idTokenString);
            
            if (idToken != null) {
                // Kiểm tra audience nếu có client ID
                if (clientId != null && !clientId.isEmpty()) {
                    if (!clientId.equals(idToken.getPayload().getAudience())) {
                        log.warn("Invalid audience: expected={}, actual={}", 
                            clientId, idToken.getPayload().getAudience());
                        return null;
                    }
                }
                
                log.info("Google ID token verified successfully for user: {}", 
                    idToken.getPayload().getEmail());
                return idToken;
            } else {
                log.warn("Google ID token verification failed: token is null");
                return null;
            }
            
        } catch (GeneralSecurityException e) {
            log.error("Security error during Google token verification", e);
            return null;
        } catch (IOException e) {
            log.error("IO error during Google token verification", e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error during Google token verification", e);
            return null;
        }
    }
    
    /**
     * Lấy thông tin user từ Google ID token
     * @param idToken Google ID token đã verified
     * @return GoogleUserInfo object
     */
    public GoogleUserInfo getUserInfo(GoogleIdToken idToken) {
        if (idToken == null) {
            return null;
        }
        
        GoogleIdToken.Payload payload = idToken.getPayload();
        
        return GoogleUserInfo.builder()
            .sub(payload.getSubject())
            .email(payload.getEmail())
            .emailVerified(payload.getEmailVerified())
            .name((String) payload.get("name"))
            .givenName((String) payload.get("given_name"))
            .familyName((String) payload.get("family_name"))
            .picture((String) payload.get("picture"))
            .locale((String) payload.get("locale"))
            .build();
    }
    
    /**
     * Verify token và trả về user info trong một lần gọi
     * @param idTokenString Google ID token string
     * @return GoogleUserInfo nếu valid, null nếu invalid
     */
    public GoogleUserInfo verifyAndGetUserInfo(String idTokenString) {
        GoogleIdToken idToken = verifyToken(idTokenString);
        return getUserInfo(idToken);
    }
    
    /**
     * DTO class để chứa thông tin user từ Google
     */
    public static class GoogleUserInfo {
        private String sub;
        private String email;
        private Boolean emailVerified;
        private String name;
        private String givenName;
        private String familyName;
        private String picture;
        private String locale;
        
        // Builder pattern
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private GoogleUserInfo userInfo = new GoogleUserInfo();
            
            public Builder sub(String sub) {
                userInfo.sub = sub;
                return this;
            }
            
            public Builder email(String email) {
                userInfo.email = email;
                return this;
            }
            
            public Builder emailVerified(Boolean emailVerified) {
                userInfo.emailVerified = emailVerified;
                return this;
            }
            
            public Builder name(String name) {
                userInfo.name = name;
                return this;
            }
            
            public Builder givenName(String givenName) {
                userInfo.givenName = givenName;
                return this;
            }
            
            public Builder familyName(String familyName) {
                userInfo.familyName = familyName;
                return this;
            }
            
            public Builder picture(String picture) {
                userInfo.picture = picture;
                return this;
            }
            
            public Builder locale(String locale) {
                userInfo.locale = locale;
                return this;
            }
            
            public GoogleUserInfo build() {
                return userInfo;
            }
        }
        
        // Getters
        public String getSub() { return sub; }
        public String getEmail() { return email; }
        public Boolean getEmailVerified() { return emailVerified; }
        public String getName() { return name; }
        public String getGivenName() { return givenName; }
        public String getFamilyName() { return familyName; }
        public String getPicture() { return picture; }
        public String getLocale() { return locale; }
        
        @Override
        public String toString() {
            return "GoogleUserInfo{" +
                "sub='" + sub + '\'' +
                ", email='" + email + '\'' +
                ", emailVerified=" + emailVerified +
                ", name='" + name + '\'' +
                ", givenName='" + givenName + '\'' +
                ", familyName='" + familyName + '\'' +
                ", picture='" + picture + '\'' +
                ", locale='" + locale + '\'' +
                '}';
        }
    }
}
