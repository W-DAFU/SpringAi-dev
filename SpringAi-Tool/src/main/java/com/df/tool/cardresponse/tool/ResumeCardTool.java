package com.df.tool.cardresponse.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 简历卡片工具。
 *
 * <p>这个工具只服务 resume 用户类型。模型需要结构化简历卡片时，
 * 可以调用这里的方法获取统一的简历卡片骨架。</p>
 */
@Component
public class ResumeCardTool {

    /**
     * 构建简历卡片基础内容。
     */
    @Tool(name = "buildResumeCard", description = "构建结构化的简历信息卡片。")
    public String buildResumeCard(
            @ToolParam(description = "候选人姓名或昵称。") String candidateName,
            @ToolParam(description = "候选人的目标岗位。") String targetRole,
            @ToolParam(description = "候选人的技能摘要。") String skills) {

        return """
                简历信息卡片
                候选人：%s
                目标岗位：%s
                技能摘要：%s
                """.formatted(candidateName, targetRole, skills);
    }
}
