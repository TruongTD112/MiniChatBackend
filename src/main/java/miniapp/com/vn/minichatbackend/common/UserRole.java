package miniapp.com.vn.minichatbackend.common;

/**
 * Enum định nghĩa các vai trò người dùng trong hệ thống
 */
public enum UserRole {
    ADMIN("admin", "Quản trị viên"),
    SELLER("seller", "Người bán"),
    CUSTOMER("customer", "Khách hàng");
    
    private final String code;
    private final String description;
    
    UserRole(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Lấy UserRole từ code
     */
    public static UserRole fromCode(String code) {
        if (code == null) return null;
        
        for (UserRole role : values()) {
            if (role.code.equalsIgnoreCase(code)) {
                return role;
            }
        }
        return null;
    }
    
    /**
     * Kiểm tra xem role có phải là admin không
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }
    
    /**
     * Kiểm tra xem role có phải là seller không
     */
    public boolean isSeller() {
        return this == SELLER;
    }
    
    /**
     * Kiểm tra xem role có phải là customer không
     */
    public boolean isCustomer() {
        return this == CUSTOMER;
    }
}
