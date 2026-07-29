package com.df.tool.cardresponse.tool;

import com.df.tool.cardresponse.domain.CardSessionPayload;
import com.df.tool.cardresponse.session.CardSessionCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 招聘者专属工具组。
 *
 * <p>招聘者能查询候选人简历，也能查询自己发布的岗位和企业信息。
 * 每个工具都只负责当前身份视角下的数据，不和求职者工具共用。</p>
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class RecruiterCardTool {

    private final CardSessionCache cardSessionCache;

    /**
     * 查询候选人简历示例。
     */
    @Tool(name = "queryRecruiterCandidateResume", description = "招聘者根据用户ID查询候选人简历示例，并缓存简历协议数据。")
    public String queryCandidateResume(@ToolParam(description = "当前请求的用户ID。") String userId) {
        String resultsJson = """
                {"name":"张三","targetRole":"Java后端开发工程师","skills":["Spring Boot","Redis","Spring AI"],"experienceYears":3,"matchScore":86}
                """;
        log.info("queryRecruiterCandidateResume: {}", resultsJson);
        cardSessionCache.save(userId, new CardSessionPayload("简历", resultsJson));
        return """
                模拟查询出的候选人简历原始数据：
                姓名：张三
                目标岗位：Java后端开发工程师
                技能：Spring Boot、Redis、Spring AI
                工作年限：3年
                匹配分：86
                """;
    }

    /**
     * 查询招聘者自己发布的岗位示例。
     */
    @Tool(name = "queryRecruiterPublishedJob", description = "招聘者根据用户ID查询自己发布的岗位示例，并缓存岗位协议数据。")
    public String queryPublishedJob(@ToolParam(description = "当前请求的用户ID。") String userId) {
        String resultsJson = """
                {"title":"Java后端开发工程师","company":"招聘方企业","requirements":["Spring Boot","微服务","Redis"],"status":"招聘中","salary":"22k-35k"}
                """;
        log.info("queryRecruiterPublishedJob: {}", resultsJson);
        cardSessionCache.save(userId, new CardSessionPayload("岗位", resultsJson));
        return """
                模拟查询出的招聘者已发布岗位原始数据：
                岗位名称：Java后端开发工程师
                企业名称：招聘方企业
                岗位要求：Spring Boot、微服务、Redis
                招聘状态：招聘中
                薪资范围：22k-35k
                """;
    }

    /**
     * 查询招聘者所属企业示例。
     */
    @Tool(name = "queryRecruiterCompany", description = "招聘者根据用户ID查询所属企业示例，并缓存企业协议数据。")
    public String queryCompany(@ToolParam(description = "当前请求的用户ID。") String userId) {
        String resultsJson = """
                {"companyName":"招聘方企业","industry":"软件服务","scale":"1000-5000人","openJobCount":12}
                """;
        log.info("queryRecruiterCompany: {}", resultsJson);
        cardSessionCache.save(userId, new CardSessionPayload("企业", resultsJson));
        return """
                模拟查询出的招聘者企业原始数据：
                企业名称：招聘方企业
                所属行业：软件服务
                企业规模：1000-5000人
                在招岗位数：12
                """;
    }
}
