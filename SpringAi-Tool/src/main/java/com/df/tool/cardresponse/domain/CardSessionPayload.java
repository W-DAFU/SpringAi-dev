package com.df.tool.cardresponse.domain;

/**
 * 临时保存在 Redis 中的协议组装数据。
 *
 * @param messageType 信息类型，例如：简历、岗位。
 * @param results 结构化业务数据，使用 JSON 字符串保存。
 */
public record CardSessionPayload(String messageType, String results) {
}
