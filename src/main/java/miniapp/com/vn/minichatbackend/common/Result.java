package miniapp.com.vn.minichatbackend.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import static miniapp.com.vn.minichatbackend.common.ErrorCode.SUCCESS;


@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {
    private  String code;
    private  boolean success;
    private  T data;
    private  String message;

    // Factory methods
    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS.getCode(), true, data, null);
    }

    public static <T> Result<T> error(@NonNull ErrorCode code, String message) {
        return new Result<>(code.getCode(), false, null, message);
    }

    public static <T> Result<T> error(@NonNull ErrorCode code) {
        return new Result<>(code.getCode(), false, null, "");
    }
}
