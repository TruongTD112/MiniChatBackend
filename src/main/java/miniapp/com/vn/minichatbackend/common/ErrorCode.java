package miniapp.com.vn.minichatbackend.common;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // Success
    SUCCESS("200", "Success"),
    
    // Client Errors (4xx)
    INVALID_REQUEST("400", "Invalid request"),
    UNAUTHORIZED("401", "Unauthorized"),
    FORBIDDEN("403", "Forbidden"),
    NOT_FOUND("404", "Resource not found"),
    METHOD_NOT_ALLOWED("405", "Method not allowed"),
    CONFLICT("409", "Conflict"),
    UNPROCESSABLE_ENTITY("422", "Unprocessable entity"),
    TOO_MANY_REQUESTS("429", "Too many requests"),
    
    // Server Errors (5xx)
    INTERNAL_ERROR("500", "Internal server error"),
    NOT_IMPLEMENTED("501", "Not implemented"),
    SERVICE_UNAVAILABLE("503", "Service unavailable"),
    
    // Business Logic Errors
    MISSING_REQUIRED_FIELDS("400", "Missing required fields"),
    INVALID_QUANTITY("400", "Quantity must be > 0"),
    PRODUCT_NOT_FOUND("404", "Product not found"),
    SHOP_NOT_FOUND("404", "Shop not found"),
    ORDER_NOT_FOUND("404", "Order not found"),
    PRODUCT_NOT_AVAILABLE("400", "Product is not available"),
    SHOP_NOT_ACTIVE("400", "Shop is not active"),
    INSUFFICIENT_STOCK("400", "Insufficient stock"),
    INVALID_ORDER_STATUS("400", "Invalid order status"),
    ORDER_CANNOT_BE_CANCELLED("400", "Only pending orders can be cancelled"),
    PRODUCT_NOT_BELONG_TO_SHOP("400", "Product does not belong to shop"),
    USER_NOT_FOUND("404", "User not found"),
    EMAIL_ALREADY_EXISTS("409", "Email already exists"),
    INVALID_CREDENTIALS("401", "Invalid credentials"),
    TOKEN_EXPIRED("401", "Token expired"),
    INVALID_TOKEN("401", "Invalid token"),
    
    // Validation Errors
    INVALID_EMAIL("400", "Invalid email format"),
    INVALID_PHONE("400", "Invalid phone number"),
    INVALID_PRICE("400", "Invalid price"),
    INVALID_COORDINATES("400", "Invalid coordinates"),
    INVALID_DATE("400", "Invalid date format"),
    INVALID_STATUS("400", "Invalid status"),
    
    // Pagination Errors
    INVALID_PAGE_NUMBER("400", "Page number must be >= 0"),
    INVALID_PAGE_SIZE("400", "Page size must be between 1 and 100"),
    
    // File Upload Errors
    FILE_TOO_LARGE("400", "File size exceeds limit"),
    INVALID_FILE_TYPE("400", "Invalid file type"),
    FILE_UPLOAD_FAILED("500", "File upload failed"),
    FILE_DELETE_FAILED("500", "File deletion failed"),
    IMAGE_NOT_FOUND("404", "Image not found"), AUTHENTICATION_FAILED("401", "not authen" );

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
