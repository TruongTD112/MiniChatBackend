package miniapp.com.vn.minichatbackend.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle validation exceptions
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        
        log.warn("Validation error for {} {}: {}", request.getMethod(), request.getRequestURI(), message);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Result.error(ErrorCode.INVALID_REQUEST, message));
    }

    /**
     * Handle constraint violation exceptions
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .findFirst()
                .orElse("Constraint violation");
        
        log.warn("Constraint violation for {} {}: {}", request.getMethod(), request.getRequestURI(), message);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Result.error(ErrorCode.INVALID_REQUEST, message));
    }

    /**
     * Handle missing request parameters
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<Void>> handleMissingParameterException(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String message = "Missing required parameter: " + ex.getParameterName();
        log.warn("Missing parameter for {} {}: {}", request.getMethod(), request.getRequestURI(), message);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Result.error(ErrorCode.MISSING_REQUIRED_FIELDS, message));
    }

    /**
     * Handle HTTP message not readable exceptions
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("HTTP message not readable for {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.OK)
                .body(Result.error(ErrorCode.INVALID_REQUEST, "Invalid request body"));
    }

    /**
     * Handle method argument type mismatch exceptions
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = "Parameter '" + ex.getName() + "' should be of type " + ex.getRequiredType().getSimpleName();
        log.warn("Type mismatch for {} {}: {}", request.getMethod(), request.getRequestURI(), message);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Result.error(ErrorCode.INVALID_REQUEST, message));
    }

    /**
     * Handle HTTP request method not supported exceptions
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String message = "Method " + ex.getMethod() + " is not supported for this endpoint";
        log.warn("Method not supported for {} {}: {}", request.getMethod(), request.getRequestURI(), message);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Result.error(ErrorCode.METHOD_NOT_ALLOWED, message));
    }

    /**
     * Handle no handler found exceptions (404)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Result<Void>> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpServletRequest request) {
        String message = "No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL();
        log.warn("No handler found for {} {}: {}", request.getMethod(), request.getRequestURI(), message);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Result.error(ErrorCode.NOT_FOUND, message));
    }

    /**
     * Handle bind exceptions
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBindException(BindException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Binding failed");
        
        log.warn("Binding error for {} {}: {}", request.getMethod(), request.getRequestURI(), message);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Result.error(ErrorCode.INVALID_REQUEST, message));
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error for {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Result.error(ErrorCode.INTERNAL_ERROR, "An unexpected error occurred"));
    }
}
