package com.df.tool.cardresponse.domain;

/**
 * 卡片返回案例的请求体。
 *
 * @param userId 用户 ID，用来隔离 Redis 中的临时会话数据。
 * @param userType 用户身份。推荐传 job_seeker/求职者 或 recruiter/招聘者。
 * @param message 用户输入的原始消息。
 */
public record CardChatRequest(String userId, String userType, String message) {
}
