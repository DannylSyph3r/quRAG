package dev.slethware.qurag.utility;

import dev.slethware.qurag.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;

public class ApiResponseUtil {

    public static <T> ApiResponse<T> successFull(String message, T data) {
        return ApiResponse.<T>builder()
                .status("success")
                .statusCode(HttpStatus.OK.value())
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return ApiResponse.<T>builder()
                .status("success")
                .statusCode(HttpStatus.CREATED.value())
                .message(message)
                .data(data)
                .build();
    }
}