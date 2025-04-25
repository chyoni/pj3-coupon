package cwchoiit.couponapi.exception;

import cwchoiit.couponapi.controller.response.ApiResponse;
import cwchoiit.couponcore.exception.CouponCoreException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CouponApiApplicationAdvice {

    @ExceptionHandler(CouponCoreException.class)
    public ResponseEntity<ApiResponse<?>> handleCouponCoreException(CouponCoreException e) {
        log.error("[handleCouponCoreException] [{}] - {} ", e.getCode(), e.getReason(), e);

        ApiResponse<Void> errorResponse = ApiResponse.error(e.getReason(), e.getCode());
        return ResponseEntity
                .status(errorResponse.getErrorStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        log.error("[handleException] ", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.internalServerError());
    }
}
