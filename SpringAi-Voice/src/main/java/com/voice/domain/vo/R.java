package com.voice.domain.vo;

import lombok.Data;

/**
 * 统一接口返回格式。
 *
 * @param <T> data 字段中承载的业务数据类型
 */
@Data
public class R<T> {

    /**
     * 业务状态码。
     * 约定 200 表示成功，其他状态码表示失败。
     */
    private Integer code;

    /**
     * 返回消息。
     * 成功时通常为 success，失败时返回具体错误原因。
     */
    private String message;

    /**
     * 请求是否处理成功。
     */
    private Boolean success;

    /**
     * 业务数据。
     * 聊天接口成功时，这里放 ChatMessageVo。
     */
    private T data;

    /**
     * 构建成功响应。
     *
     * @param data 业务数据
     * @param <T>  业务数据类型
     * @return 统一成功响应
     */
    public static <T> R<T> ok(T data) {
        R<T> result = new R<>();
        result.setCode(200);
        result.setMessage("success");
        result.setSuccess(true);
        result.setData(data);
        return result;
    }

    /**
     * 构建失败响应。
     *
     * @param code    错误状态码
     * @param message 错误说明
     * @param <T>     业务数据类型
     * @return 统一失败响应
     */
    public static <T> R<T> fail(Integer code, String message) {
        R<T> result = new R<>();
        result.setCode(code);
        result.setMessage(message);
        result.setSuccess(false);
        result.setData(null);
        return result;
    }

}
