package com.realtimevideo.domain.vo;

public class R<T> {

    private Integer code;
    private String message;
    private Boolean success;
    private T data;

    public static <T> R<T> ok(T data) {
        R<T> result = new R<>();
        result.setCode(200);
        result.setMessage("success");
        result.setSuccess(true);
        result.setData(data);
        return result;
    }

    public static <T> R<T> fail(Integer code, String message) {
        R<T> result = new R<>();
        result.setCode(code);
        result.setMessage(message);
        result.setSuccess(false);
        return result;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
