package com.voice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voice.commons.domain.vo.ChatStreamBlockVo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 流式聊天 NDJSON 解析器。
 * <p>
 * 模型会按 token 流式输出，单个 JSON 行可能被拆成多段。
 * 该解析器按真实换行符切分 block：只有遇到真实换行后，才认为这一行 JSON 完整。
 * JSON 字符串字段中的换行必须由模型输出为转义字符 \\n，不能输出真实换行。
 */
public class ChatStreamBlockParser {

    /**
     * JSON 解析器。
     */
    private final ObjectMapper objectMapper;

    /**
     * 当前请求的流式缓冲区。
     */
    private final StringBuilder buffer = new StringBuilder();

    public ChatStreamBlockParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 接收一段模型输出，并返回已经完整解析出来的 block。
     *
     * @param chunk 模型本次返回的文本增量
     * @return 已经遇到真实换行、可以安全返回给前端的 block 列表
     */
    public List<ChatStreamBlockVo> accept(String chunk) throws IOException {
        buffer.append(chunk);

        List<ChatStreamBlockVo> blocks = new ArrayList<>();
        int newlineIndex;
        while ((newlineIndex = buffer.indexOf("\n")) >= 0) {
            String line = buffer.substring(0, newlineIndex).trim();
            buffer.delete(0, newlineIndex + 1);

            if (!line.isEmpty()) {
                blocks.add(parseJsonLine(line));
            }
        }

        return blocks;
    }

    /**
     * 模型流结束时处理最后一行。
     * <p>
     * 有些模型最后不会输出真实换行，如果不 flush 会丢掉最后一个 block。
     */
    public Optional<ChatStreamBlockVo> finish() throws IOException {
        String line = buffer.toString().trim();
        buffer.setLength(0);

        if (line.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(parseJsonLine(line));
    }

    /**
     * 把一行完整 NDJSON 转成后端返回结构。
     */
    private ChatStreamBlockVo parseJsonLine(String line) throws IOException {
        JsonNode node = objectMapper.readTree(line);
        String type = requiredText(node, "type");

        ChatStreamBlockVo block = new ChatStreamBlockVo();
        block.setCardType(type);

        switch (type) {
            case "text" -> block.setAnswerText(requiredText(node, "text"));
            case "product_card" -> block.setAnswerText(requiredText(node, "query"));
            default -> throw new IOException("不支持的流式 block 类型：" + type);
        }

        return block;
    }

    /**
     * 读取必填字符串字段。
     */
    private String requiredText(JsonNode node, String fieldName) throws IOException {
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull()) {
            throw new IOException("缺少必填流式 block 字段：" + fieldName);
        }
        return field.asText();
    }

}
