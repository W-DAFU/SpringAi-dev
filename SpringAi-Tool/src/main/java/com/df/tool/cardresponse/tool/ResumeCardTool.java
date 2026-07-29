package com.df.tool.cardresponse.tool;

import com.df.tool.cardresponse.domain.CardSessionPayload;
import com.df.tool.cardresponse.session.CardSessionCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 简历卡片工具。
 *
 * <p>工具负责两件事：
 * 1. 把后续协议组装需要的结构化数据写入 Redis；
 * 2. 返回模拟查询出的原始数据给 AI 模型，让模型基于原始数据回复自然语言。</p>
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ResumeCardTool {

    private final CardSessionCache cardSessionCache;

    /**
     * 查询一份简历示例原始数据。
     */
    @Tool(name = "queryResumeExample", description = "根据用户ID查询一份简历示例原始数据，并缓存简历协议数据。")
    public String queryResumeExample(@ToolParam(description = "当前请求的用户ID。") String userId) {
        String resultsJson = """
                {"name":"张三","targetRole":"Java后端开发工程师","skills":["Spring Boot","Redis","Spring AI"],"experienceYears":3}
                """;
        log.info("queryResumeExample: {}", resultsJson);
        cardSessionCache.save(userId, new CardSessionPayload("简历", resultsJson));
        return """
                模拟查询出的简历原始数据：
                姓名：张三
                目标岗位：Java后端开发工程师
                技能：Spring Boot、Redis、Spring AI
                工作年限：3年
                """;
    }
}
