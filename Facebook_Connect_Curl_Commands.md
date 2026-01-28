# Facebook Connect - CURL Commands

## Cấu hình
- Base URL: `http://localhost:8080`
- API `/facebook/**` không yêu cầu Authorization (permitAll)

---

## Connect Facebook Page (Kết nối Facebook Page vào Business)

**Endpoint:** `POST /facebook/connect`

**Body:**
- `userToken` (bắt buộc): Facebook user access token từ client sau khi đăng nhập Facebook
- `pageId` (bắt buộc): Page ID của Facebook Page cần connect
- `businessId` (bắt buộc): ID của Business để gán channel vào

### Curl mẫu

```bash
curl -X POST http://localhost:8080/facebook/connect \
  -H "Content-Type: application/json" \
  -d '{
    "userToken": "EAABwzLix...",
    "pageId": "123456789012345",
    "businessId": 1
  }'
```

### Ví dụ với giá trị thật

```bash
curl -X POST http://localhost:8080/facebook/connect \
  -H "Content-Type: application/json" \
  -d "{\"userToken\": \"YOUR_FACEBOOK_USER_ACCESS_TOKEN\", \"pageId\": \"YOUR_PAGE_ID\", \"businessId\": 1}"
```

**Lưu ý:**
- `userToken`: Lấy từ Facebook Login flow phía client (cần quyền `pages_show_list`, `pages_manage_metadata`, `pages_messaging`… tùy chức năng).
- `pageId`: Lấy từ API `POST /facebook/pages` (danh sách pages user quản lý).
- `businessId`: ID business trong DB (có thể lấy từ API business).

### Response thành công (200)

```json
{
  "code": 0,
  "message": "Success",
  "data": {
    "channelId": 1,
    "channelIdString": "123456789012345",
    "name": "My Business Page",
    "avatarUrl": "https://scontent.xx.fbcdn.net/v/...",
    "platform": "FACEBOOK",
    "status": 1
  }
}
```

### Một số lỗi thường gặp

| HTTP | code/message | Ý nghĩa |
|------|--------------|---------|
| 400  | userToken, pageId và businessId không được để trống | Thiếu field bắt buộc |
| 401  | Facebook token không hợp lệ hoặc đã hết hạn | userToken sai hoặc hết hạn |
| 404  | Không tìm thấy Business hoặc Page | businessId/pageId không tồn tại |
| 409  | Channel đã được connect vào Business này rồi | Page này đã connect vào business |
| 500  | Lỗi Facebook API / mã hóa | Lỗi phía server hoặc Facebook |
