package com.df.tool.cardresponse.tool;

import com.df.tool.cardresponse.domain.CardSessionPayload;
import com.df.tool.cardresponse.session.CardSessionCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 岗位卡片工具。
 *
 * <p>工具负责两件事：
 * 1. 把后续协议组装需要的结构化数据写入 Redis；
 * 2. 返回模拟查询出的原始数据给 AI 模型，让模型基于原始数据回复自然语言。</p>
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class JobCardTool {

    private final CardSessionCache cardSessionCache;

    /**
     * 查询一份岗位示例原始数据。
     */
    @Tool(name = "queryJobExample", description = "根据用户ID查询一份岗位示例原始数据，并缓存岗位协议数据。")
    public String queryJobExample(@ToolParam(description = "当前请求的用户ID。") String userId) {
        String resultsJson = """
                {"title":"Java后端开发工程师","company":"示例科技","requirements":["Spring Boot","Redis","AI应用开发"],"salary":"20k-30k"}
                """;
        log.info("queryJobExample: {}", resultsJson);
        cardSessionCache.save(userId, new CardSessionPayload("岗位", resultsJson));
        return """
                模拟查询出的岗位原始数据：
                岗位名称：Java后端开发工程师
                公司名称：示例科技
                核心要求：Spring Boot、Redis、AI应用开发
                薪资范围：20k-30k
                """;
    }
}
