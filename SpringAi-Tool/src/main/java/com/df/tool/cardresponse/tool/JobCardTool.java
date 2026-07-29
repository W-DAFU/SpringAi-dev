package com.df.tool.cardresponse.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 岗位卡片工具。
 *
 * <p>这个工具只服务 job 用户类型。模型需要结构化岗位卡片时，
 * 可以调用这里的方法获取统一的岗位卡片骨架。</p>
 */
@Component
public class JobCardTool {

    /**
     * 构建岗位卡片基础内容。
     */
    @Tool(name = "buildJobCard", description = "构建结构化的岗位信息卡片。")
    public String buildJobCard(
            @ToolParam(description = "岗位名称。") String title,
            @ToolParam(description = "公司名称。") String company,
            @ToolParam(description = "岗位核心要求。") String requirements) {
        return """
                岗位信息卡片
                岗位名称：%s
                公司名称：%s
                核心要求：%s
                """.formatted(title, company, requirements);
    }
}
