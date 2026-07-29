package com.df.tool.cardresponse.prompt;

import com.df.tool.cardresponse.domain.CardUserType;
import org.springframework.stereotype.Component;

/**
 * 根据用户类型提供对应的系统提示词。
 */
@Component
public class CardPromptProvider {

    private static final String RESUME_PROMPT = """
            你是简历信息卡片助手。
            请阅读用户消息，生成简洁、结构化的简历信息卡片。
            当需要整理候选人姓名、目标岗位、技能摘要等字段时，优先调用简历工具。
            最终回答必须使用中文，并以清晰的卡片分区展示。
            """;

    private static final String JOB_PROMPT = """
            你是岗位信息卡片助手。
            请阅读用户消息，生成简洁、结构化的岗位信息卡片。
            当需要整理岗位名称、公司名称、核心要求等字段时，优先调用岗位工具。
            最终回答必须使用中文，并以清晰的卡片分区展示。
            """;

    /**
     * 根据用户类型选择提示词，避免把不同场景的规则混在一起。
     */
    public String getPrompt(CardUserType userType) {
        return switch (userType) {
            case RESUME -> RESUME_PROMPT;
            case JOB -> JOB_PROMPT;
        };
    }
}
