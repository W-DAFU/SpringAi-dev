package com.df.tool.cardresponse.domain;

/**
 * 返回给前端的完整协议。
 *
 * @param messageType 信息类型，例如：简历、岗位、企业。
 * @param results 从 Redis 缓存中取出的结构化 JSON 数据。
 * @param message 模型生成的一段中文自然语言说明。
 */
public record CardChatResponse(String messageType, String results, String message) {
}
