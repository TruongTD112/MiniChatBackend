# Business CRUD - CURL Commands

## Cấu hình
- Base URL: `http://localhost:8080`
- Thay `YOUR_TOKEN` bằng JWT token nếu có authentication
- Thay `BUSINESS_ID` bằng ID business thực tế sau khi tạo

---

## 1. Create Business (Tạo business mới)

### Tạo business với đầy đủ thông tin:
```bash
curl -X POST http://localhost:8080/api/business \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Cửa hàng ABC",
    "phone": "0123456789",
    "address": "123 Đường ABC, Quận 1, TP.HCM",
    "description": "Cửa hàng bán lẻ các sản phẩm tiêu dùng",
    "status": 1,
    "metadata": "{\"category\":\"retail\",\"established\":\"2020\"}",
    "style": "{\"theme\":\"modern\",\"color\":\"blue\"}"
  }'
```

### Tạo business tối giản (chỉ có name - bắt buộc):
```bash
curl -X POST http://localhost:8080/api/business \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Business Test"
  }'
```

---

## 2. Get All Businesses (Lấy danh sách tất cả business)

```bash
curl -X GET http://localhost:8080/api/business \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 3. Get Business by ID (Lấy business theo ID)

```bash
curl -X GET http://localhost:8080/api/business/BUSINESS_ID \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Ví dụ:**
```bash
curl -X GET http://localhost:8080/api/business/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 4. Update Business (Cập nhật business)

### Cập nhật đầy đủ thông tin:
```bash
curl -X PUT http://localhost:8080/api/business/BUSINESS_ID \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Cửa hàng ABC - Updated",
    "phone": "0987654321",
    "address": "456 Đường XYZ, Quận 2, TP.HCM",
    "description": "Mô tả đã được cập nhật",
    "status": 1,
    "metadata": "{\"category\":\"retail\",\"established\":\"2020\",\"updated\":\"2024\"}",
    "style": "{\"theme\":\"classic\",\"color\":\"green\"}"
  }'
```

### Cập nhật một phần thông tin:
```bash
curl -X PUT http://localhost:8080/api/business/BUSINESS_ID \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Tên mới",
    "status": 0
  }'
```

**Ví dụ:**
```bash
curl -X PUT http://localhost:8080/api/business/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Business Updated",
    "phone": "0987654321"
  }'
```

---

## 5. Delete Business (Xóa business)

```bash
curl -X DELETE http://localhost:8080/api/business/BUSINESS_ID \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Ví dụ:**
```bash
curl -X DELETE http://localhost:8080/api/business/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## Lưu ý:

1. **Authentication**: Tất cả endpoint yêu cầu authentication. Thay `YOUR_TOKEN` bằng JWT token thực tế.

2. **Business ID**: Sau khi tạo business thành công, copy `id` từ response và thay `BUSINESS_ID` trong các lệnh tiếp theo.

3. **Status**: 
   - `1` = active
   - `0` = inactive

4. **Metadata và Style**: Là JSON string, cần escape dấu ngoặc kép bên trong.

5. **Validation**: 
   - `name` là bắt buộc khi tạo business
   - `name` tối đa 255 ký tự
   - `phone` tối đa 20 ký tự
   - `address` tối đa 255 ký tự

---

## Ví dụ workflow hoàn chỉnh:

```bash
# 1. Tạo business mới
RESPONSE=$(curl -X POST http://localhost:8080/api/business \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Test Business",
    "phone": "0123456789",
    "address": "123 Test Street",
    "status": 1
  }')

# 2. Lấy ID từ response (cần parse JSON)
# BUSINESS_ID=$(echo $RESPONSE | jq -r '.data.id')

# 3. Lấy thông tin business vừa tạo
# curl -X GET http://localhost:8080/api/business/$BUSINESS_ID \
#   -H "Authorization: Bearer YOUR_TOKEN"

# 4. Cập nhật business
# curl -X PUT http://localhost:8080/api/business/$BUSINESS_ID \
#   -H "Content-Type: application/json" \
#   -H "Authorization: Bearer YOUR_TOKEN" \
#   -d '{"name": "Updated Business Name"}'

# 5. Xóa business
# curl -X DELETE http://localhost:8080/api/business/$BUSINESS_ID \
#   -H "Authorization: Bearer YOUR_TOKEN"
```

