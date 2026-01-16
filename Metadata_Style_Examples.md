# Ví dụ Metadata và Style cho Business và Product

## 1. Business - Metadata

Metadata của Business lưu các thông tin khác như chính sách, chương trình khuyến mãi dạng JSON.

### Ví dụ đầy đủ:
```json
{
  "policies": {
    "returnPolicy": "Đổi trả trong 7 ngày kể từ ngày nhận hàng",
    "warranty": "Bảo hành 12 tháng cho tất cả sản phẩm",
    "shipping": "Miễn phí vận chuyển đơn hàng trên 500.000đ",
    "payment": "Chấp nhận thanh toán COD, chuyển khoản, thẻ tín dụng"
  },
  "promotions": [
    {
      "name": "Giảm 20% tháng 1",
      "description": "Áp dụng cho tất cả sản phẩm",
      "startDate": "2024-01-01",
      "endDate": "2024-01-31",
      "discount": 20
    },
    {
      "name": "Mua 2 tặng 1",
      "description": "Áp dụng cho sản phẩm áo thun",
      "startDate": "2024-02-01",
      "endDate": "2024-02-29"
    }
  ],
  "contact": {
    "email": "support@business.com",
    "hotline": "1900-xxxx",
    "workingHours": "8:00 - 22:00"
  }
}
```

### Ví dụ tối giản:
```json
{
  "policies": {
    "returnPolicy": "Đổi trả trong 7 ngày"
  },
  "promotions": []
}
```

---

## 2. Business - Style

Style của Business lưu style mong muốn của cửa hàng như tone, màu sắc, font, ngôn ngữ dạng JSON.

### Ví dụ đầy đủ:
```json
{
  "tone": "thân thiện",
  "color": {
    "primary": "#FF6B6B",
    "secondary": "#4ECDC4",
    "accent": "#FFE66D",
    "background": "#FFFFFF",
    "text": "#2C3E50"
  },
  "font": {
    "family": "Arial",
    "size": "14px",
    "weight": "normal"
  },
  "language": "vi",
  "emoji": true,
  "greeting": "Xin chào! Tôi có thể giúp gì cho bạn?",
  "closing": "Cảm ơn bạn đã liên hệ! Chúc bạn một ngày tốt lành!"
}
```

### Ví dụ tối giản:
```json
{
  "tone": "chuyên nghiệp",
  "color": {
    "primary": "#2C3E50"
  },
  "language": "vi"
}
```

### Các giá trị tone phổ biến:
- `"thân thiện"` - Giọng điệu thân thiện, gần gũi
- `"chuyên nghiệp"` - Giọng điệu chuyên nghiệp, lịch sự
- `"vui vẻ"` - Giọng điệu vui vẻ, năng động
- `"trẻ trung"` - Giọng điệu trẻ trung, hiện đại
- `"cổ điển"` - Giọng điệu cổ điển, trang trọng

---

## 3. Product - Status

Trạng thái sản phẩm có thể là:
- `"1"` hoặc `"available"` - Còn hàng
- `"2"` hoặc `"sold_out"` - Hết hàng
- `"3"` hoặc `"no_longer_sell"` - Ngừng bán

### Ví dụ:
```json
"1"        // Còn hàng
"available" // Còn hàng
"2"        // Hết hàng
"sold_out" // Hết hàng
"3"        // Ngừng bán
"no_longer_sell" // Ngừng bán
```

---

## 4. Product - Metadata

Metadata của Product lưu các thuộc tính khác của sản phẩm như kích thước, màu sắc, chất liệu, thông số kỹ thuật dạng JSON.

### Ví dụ đầy đủ (Áo thun):
```json
{
  "size": ["S", "M", "L", "XL", "XXL"],
  "color": ["Đỏ", "Xanh dương", "Đen", "Trắng"],
  "material": "Cotton 100%",
  "origin": "Việt Nam",
  "specifications": {
    "weight": "200g",
    "dimensions": "30x40cm",
    "care": "Giặt máy ở nhiệt độ thấp, không tẩy"
  },
  "tags": ["bestseller", "new", "trending"],
  "category": "Áo thun",
  "brand": "Local Brand",
  "warranty": "6 tháng"
}
```

### Ví dụ đầy đủ (Điện thoại):
```json
{
  "specifications": {
    "screen": "6.7 inch OLED",
    "processor": "Snapdragon 8 Gen 2",
    "ram": "12GB",
    "storage": "256GB",
    "camera": "50MP + 12MP + 12MP",
    "battery": "5000mAh",
    "os": "Android 14"
  },
  "color": ["Đen", "Trắng", "Xanh"],
  "tags": ["flagship", "5G", "wireless-charging"],
  "category": "Điện thoại",
  "brand": "Samsung",
  "warranty": "12 tháng",
  "accessories": ["Sạc nhanh", "Ốp lưng", "Tai nghe"]
}
```

### Ví dụ tối giản:
```json
{
  "size": ["M", "L"],
  "color": ["Đỏ", "Xanh"],
  "material": "Cotton"
}
```

---

## Lưu ý khi sử dụng:

1. **JSON phải hợp lệ**: Đảm bảo JSON string được escape đúng khi gửi qua API
2. **Encoding**: Sử dụng UTF-8 để hỗ trợ tiếng Việt
3. **Validation**: Nên validate JSON structure ở tầng application nếu cần
4. **Escape trong curl**: Khi dùng curl, cần escape dấu ngoặc kép:
   ```bash
   -d '{"metadata": "{\"key\": \"value\"}"}'
   ```

