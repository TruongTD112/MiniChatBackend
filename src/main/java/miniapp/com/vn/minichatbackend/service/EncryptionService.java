package miniapp.com.vn.minichatbackend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Service để mã hóa và giải mã dữ liệu nhạy cảm như access tokens
 */
@Slf4j
@Service
public class EncryptionService {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";
    
    private final SecretKeySpec secretKey;
    
    /**
     * Constructor nhận secret key từ configuration
     * 
     * Thứ tự ưu tiên:
     * 1. Environment Variable: APP_ENCRYPTION_SECRET_KEY (Spring tự động map sang app.encryption.secret-key)
     * 2. application.properties: app.encryption.secret-key
     * 3. Default value (chỉ dùng cho development)
     * 
     * Cách cấu hình:
     * - Environment Variable (khuyến nghị cho production):
     *   export APP_ENCRYPTION_SECRET_KEY="your-secret-key-here"
     * 
     * - application.properties:
     *   app.encryption.secret-key=your-secret-key-here
     */
    public EncryptionService(@Value("${app.encryption.secret-key:mySecretKey123456789012345678901234}") String secretKeyString) {
        // Đảm bảo key có đúng 32 bytes cho AES-256 hoặc 16 bytes cho AES-128
        byte[] keyBytes = secretKeyString.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 16) {
            // Pad key nếu quá ngắn
            byte[] paddedKey = new byte[16];
            System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 16));
            this.secretKey = new SecretKeySpec(paddedKey, ALGORITHM);
        } else if (keyBytes.length < 32) {
            // Sử dụng 16 bytes đầu cho AES-128
            byte[] key16 = new byte[16];
            System.arraycopy(keyBytes, 0, key16, 0, 16);
            this.secretKey = new SecretKeySpec(key16, ALGORITHM);
        } else {
            // Sử dụng 32 bytes đầu cho AES-256
            byte[] key32 = new byte[32];
            System.arraycopy(keyBytes, 0, key32, 0, 32);
            this.secretKey = new SecretKeySpec(key32, ALGORITHM);
        }
    }
    
    /**
     * Mã hóa chuỗi text
     * @param plainText Text cần mã hóa
     * @return Text đã mã hóa dạng Base64
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("Error encrypting text", e);
            throw new RuntimeException("Lỗi khi mã hóa dữ liệu", e);
        }
    }
    
    /**
     * Giải mã chuỗi text đã mã hóa
     * @param encryptedText Text đã mã hóa dạng Base64
     * @return Text đã giải mã
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error decrypting text", e);
            throw new RuntimeException("Lỗi khi giải mã dữ liệu", e);
        }
    }
}
