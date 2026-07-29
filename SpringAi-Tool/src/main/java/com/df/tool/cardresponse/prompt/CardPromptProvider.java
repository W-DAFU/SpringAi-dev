package com.df.tool.cardresponse.prompt;

import com.df.tool.cardresponse.domain.CardUserType;
import org.springframework.stereotype.Component;

/**
 * 根据用户身份提供对应的系统提示词。
 */
@Component
public class CardPromptProvider {

    private static final String COMMON_RULES = """
            只输出一段简洁的中文自然语言说明，不要输出 Markdown，不要输出标题，不要输出列表，不要输出 JSON。
            结构化协议数据由后端从 Redis 缓存中组装，你只需要根据工具返回的原始数据解释结果。
            如果需要调用工具，必须把系统消息中的当前用户ID原样传给工具。
            """;

    private static final String JOB_SEEKER_PROMPT = """
            你是求职者服务助手。
            当前用户身份是求职者，不是简历信息类型。
            求职者需要查看适合自己的岗位信息和企业信息。
            可以调用求职者专属工具：查询推荐岗位、查询目标企业。
            不要调用招聘者视角的简历筛选工具。
            """ + COMMON_RULES;

    private static final String RECRUITER_PROMPT = """
            你是招聘者服务助手。
            当前用户身份是招聘者，不是岗位信息类型。
            招聘者需要查看候选人简历，也可以查看自己发布的岗位和企业信息。
            可以调用招聘者专属工具：查询候选人简历、查询自己发布的岗位、查询企业信息。
            不要调用求职者视角的岗位推荐工具。
            """ + COMMON_RULES;

    /**
     * 按用户身份选择提示词，避免把“用户身份”和“返回信息类型”混在一起。
     */
    public String getPrompt(CardUserType userType) {
        return switch (userType) {
            case JOB_SEEKER -> JOB_SEEKER_PROMPT;
            case RECRUITER -> RECRUITER_PROMPT;
        };
    }
}
