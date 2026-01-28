# Hướng dẫn cấu hình Encryption Key

## Tổng quan

Key mã hóa được sử dụng để mã hóa Page Access Token trước khi lưu vào database. Key này được lấy theo thứ tự ưu tiên sau:

## Thứ tự ưu tiên (từ cao đến thấp):

1. **Environment Variable** (khuyến nghị cho production)
2. **application.properties** (dùng cho development)
3. **Default value** (chỉ dùng khi test)

## Cách cấu hình:

### 1. Sử dụng Environment Variable (Khuyến nghị cho Production)

**Windows (PowerShell):**
```powershell
$env:APP_ENCRYPTION_SECRET_KEY="your-very-long-secret-key-at-least-32-characters"
```

**Windows (CMD):**
```cmd
set APP_ENCRYPTION_SECRET_KEY=your-very-long-secret-key-at-least-32-characters
```

**Linux/Mac:**
```bash
export APP_ENCRYPTION_SECRET_KEY="your-very-long-secret-key-at-least-32-characters"
```

**Docker:**
```bash
docker run -e APP_ENCRYPTION_SECRET_KEY="your-secret-key" ...
```

**Railway/Heroku:**
- Vào Settings → Environment Variables
- Thêm: `APP_ENCRYPTION_SECRET_KEY` = `your-secret-key`

### 2. Sử dụng application.properties (Development)

Mở file `src/main/resources/application.properties` và thay đổi:

```properties
app.encryption.secret-key=your-very-long-secret-key-at-least-32-characters
```

⚠️ **LƯU Ý:** KHÔNG commit key thật vào git! Chỉ dùng key test trong development.

### 3. Tạo key ngẫu nhiên an toàn

**Sử dụng OpenSSL:**
```bash
openssl rand -base64 32
```

**Sử dụng Java:**
```java
import java.security.SecureRandom;
import java.util.Base64;

SecureRandom random = new SecureRandom();
byte[] key = new byte[32]; // 32 bytes = 256 bits
random.nextBytes(key);
String keyString = Base64.getEncoder().encodeToString(key);
System.out.println(keyString);
```

**Online generator:**
- https://www.random.org/strings/
- Chọn: 32 characters, alphanumeric

## Yêu cầu về Key:

- **Độ dài tối thiểu:** 16 ký tự (cho AES-128)
- **Độ dài khuyến nghị:** 32 ký tự (cho AES-256)
- **Nên chứa:** Chữ cái, số, ký tự đặc biệt
- **Không nên:** Dùng key dễ đoán, key ngắn, key có trong code

## Bảo mật:

1. ✅ **Lưu key trong Environment Variable** cho production
2. ✅ **Không commit key vào git**
3. ✅ **Sử dụng key khác nhau** cho mỗi môi trường (dev, staging, production)
4. ✅ **Rotate key định kỳ** (thay đổi key sau một khoảng thời gian)
5. ❌ **KHÔNG** hardcode key trong code
6. ❌ **KHÔNG** chia sẻ key qua email/chat

## Kiểm tra key đang được sử dụng:

Thêm log tạm thời trong `EncryptionService` constructor để kiểm tra:

```java
log.info("Encryption key length: {}", secretKeyString.length());
log.info("Encryption key prefix: {}", secretKeyString.substring(0, Math.min(4, secretKeyString.length())) + "***");
```

⚠️ **Xóa log này sau khi kiểm tra!**
