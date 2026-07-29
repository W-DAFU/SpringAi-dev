package com.df.tool.cardresponse.domain;

/**
 * 卡片返回案例的请求体。
 *
 * @param userType 用户类型。支持 resume 和 job，用来决定加载哪套提示词和工具。
 * @param message 用户输入的原始消息。
 */
public record CardChatRequest(String userType, String message) {
}
