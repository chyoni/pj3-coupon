package cwchoiit.couponapi.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(NON_NULL)
public class ApiResponse <T> {
    private boolean ok;
    private T data;
    private String reason;
    private String errorCode;
    private HttpStatus errorStatus;

    public ApiResponse(boolean ok, T data, String reason, String errorCode) {
        this.ok = ok;
        this.data = data;
        this.reason = reason;
        this.errorCode = errorCode;

        generateHttpStatus(errorCode);
    }

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.ok = true;
        response.data = data;
        return response;
    }

    public static <Void> ApiResponse<Void> ok() {
        ApiResponse<Void> response = new ApiResponse<>();
        response.ok = true;
        return response;
    }

    public static ApiResponse<Void> error(String reason, String errorCode) {
        ApiResponse<Void> response = new ApiResponse<>();
        response.ok = false;
        response.reason = reason;
        response.errorCode = errorCode;
        response.generateHttpStatus(errorCode);
        return response;
    }

    public static ApiResponse<Void> internalServerError() {
        ApiResponse<Void> response = new ApiResponse<>();
        response.ok = false;
        response.reason = "Something went wrong. Please try again later.";
        return response;
    }

    private void generateHttpStatus(String errorCode) {
        if ("COUPON-C-0001".equals(errorCode)) {
            this.errorStatus = HttpStatus.NO_CONTENT;
        }
        if ("COUPON-C-0002".equals(errorCode)) {
            this.errorStatus = HttpStatus.NO_CONTENT;
        }
        if ("COUPON-C-0003".equals(errorCode)) {
            this.errorStatus = HttpStatus.NO_CONTENT;
        }
        if ("COUPON-C-0004".equals(errorCode)) {
            this.errorStatus = HttpStatus.NO_CONTENT;
        }
    }
}
