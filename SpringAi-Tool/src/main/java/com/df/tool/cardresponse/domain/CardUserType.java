package com.df.tool.cardresponse.domain;

import java.util.Arrays;

/**
 * 卡片案例支持的用户类型。
 */
public enum CardUserType {

    /**
     * 简历场景：面向求职者，根据用户输入生成简历信息卡片。
     */
    RESUME("resume"),

    /**
     * 岗位场景：面向招聘方，根据用户输入生成岗位信息卡片。
     */
    JOB("job");

    private final String value;

    CardUserType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    /**
     * 返回最终协议中的中文信息类型。
     */
    public String messageType() {
        return switch (this) {
            case RESUME -> "简历";
            case JOB -> "岗位";
        };
    }

    /**
     * 把请求中的 userType 转成枚举，集中处理非法类型。
     */
    public static CardUserType from(String value) {
        return Arrays.stream(values())
                .filter(type -> type.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported userType: " + value + ". Supported values are resume and job."));
    }
}
