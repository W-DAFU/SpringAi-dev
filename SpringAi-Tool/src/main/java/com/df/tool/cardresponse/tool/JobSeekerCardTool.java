package com.df.tool.cardresponse.tool;

import com.df.tool.cardresponse.domain.CardSessionPayload;
import com.df.tool.cardresponse.session.CardSessionCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 求职者专属工具组。
 *
 * <p>求职者能查询岗位和企业。工具内部保存给后端组装协议用的 JSON，
 * 返回给 AI 模型的是模拟查询出的原始业务数据。</p>
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class JobSeekerCardTool {

    private final CardSessionCache cardSessionCache;

    /**
     * 查询推荐岗位示例。
     */
    @Tool(name = "queryJobSeekerRecommendedJob", description = "求职者根据用户ID查询推荐岗位示例，并缓存岗位协议数据。")
    public String queryRecommendedJob(@ToolParam(description = "当前请求的用户ID。") String userId) {
        String resultsJson = """
                {"title":"Java后端开发工程师","company":"示例科技","requirements":["Spring Boot","Redis","AI应用开发"],"salary":"20k-30k","matchReason":"技能栈匹配度高"}
                """;
        log.info("queryJobSeekerRecommendedJob: {}", resultsJson);
        cardSessionCache.save(userId, new CardSessionPayload("岗位", resultsJson));
        return """
                模拟查询出的求职者推荐岗位原始数据：
                岗位名称：Java后端开发工程师
                企业名称：示例科技
                核心要求：Spring Boot、Redis、AI应用开发
                薪资范围：20k-30k
                推荐原因：求职者技能栈匹配度高
                """;
    }

    /**
     * 查询目标企业示例。
     */
    @Tool(name = "queryJobSeekerCompany", description = "求职者根据用户ID查询目标企业示例，并缓存企业协议数据。")
    public String queryCompany(@ToolParam(description = "当前请求的用户ID。") String userId) {
        String resultsJson = """
                {"companyName":"示例科技","industry":"企业服务","scale":"500-1000人","benefits":["五险一金","弹性工作","技术分享"]}
                """;
        log.info("queryJobSeekerCompany: {}", resultsJson);
        cardSessionCache.save(userId, new CardSessionPayload("企业", resultsJson));
        return """
                模拟查询出的求职者关注企业原始数据：
                企业名称：示例科技
                所属行业：企业服务
                企业规模：500-1000人
                企业福利：五险一金、弹性工作、技术分享
                """;
    }
}
