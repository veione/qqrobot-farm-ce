package de.honoka.qqrobot.farm.web.common;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ApiResponse<T> {

    private Integer code;

    private Boolean status;

    private String msg;

    private T data;

    public ApiResponse(Integer code, Boolean status, String msg, T data) {
        this.code = code;
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    public static <T1> ApiResponse<T1> success(String msg, T1 data) {
        return new ApiResponse<>(0, true, msg, data);
    }

    public static <T1> ApiResponse<T1> fail(String msg) {
        return new ApiResponse<>(-1, false, msg, null);
    }
}
