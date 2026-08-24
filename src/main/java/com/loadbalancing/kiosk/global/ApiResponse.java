package com.loadbalancing.kiosk.global;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T>{

    private static final String SUCCESS = "SUCCESS";

    private boolean success;
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data){
        return ApiResponse.<T>builder().success(true).code(200).message("").data(data).build();
    }

    public static <T> ApiResponse<T> success(int code, String message){
        return ApiResponse.<T>builder().success(true).code(code).message(message).data(null).build();
    }

    public static ApiResponse<Void> noContentSuccess() {
        return ApiResponse.<Void>builder().success(true).message("").code(204).data(null).build();
    }

    public static ApiResponse<Void> noContentSuccess(String message) {
        return ApiResponse.<Void>builder().success(true).message(message).code(204).data(null).build();
    }

    public static <T> ApiResponse<T> error(int code, String message, T data) {
        return ApiResponse.<T>builder().success(false).code(code).message(message).data(data).build();
    }

    public static  ApiResponse<Void> error(int code, String message) {
        return ApiResponse.<Void>builder().success(false).code(code).message(message).data(null).build();
    }

}
