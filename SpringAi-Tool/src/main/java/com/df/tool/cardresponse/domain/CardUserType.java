package com.df.tool.cardresponse.domain;

import java.util.Arrays;
import java.util.Set;

/**
 * 卡片案例支持的用户身份类型。
 *
 * <p>这里的 userType 表示“谁在使用系统”，不是最终返回的 messageType。
 * 求职者通常查询岗位、企业；招聘者通常查询候选人简历，也可以查询自己发布的岗位和企业信息。</p>
 */
public enum CardUserType {

    /**
     * 求职者身份：面向找工作的人，允许使用求职者专属的岗位、企业工具。
     */
    JOB_SEEKER(Set.of("job_seeker", "seeker", "candidate", "求职者", "resume")),

    /**
     * 招聘者身份：面向招聘方，允许使用招聘者专属的简历、已发布岗位、企业工具。
     */
    RECRUITER(Set.of("recruiter", "hr", "company", "招聘者", "job"));

    private final Set<String> aliases;

    CardUserType(Set<String> aliases) {
        this.aliases = aliases;
    }

    /**
     * 当模型没有调用工具时，用这个类型兜底组装协议。
     */
    public String defaultMessageType() {
        return switch (this) {
            case JOB_SEEKER -> "岗位";
            case RECRUITER -> "简历";
        };
    }

    /**
     * 把请求中的 userType 转成用户身份枚举，集中处理非法类型。
     */
    public static CardUserType from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Unsupported userType: " + value
                    + ". Supported values are job_seeker/求职者 and recruiter/招聘者.");
        }
        String normalized = value.trim();
        return Arrays.stream(values())
                .filter(type -> type.aliases.stream().anyMatch(alias -> alias.equalsIgnoreCase(normalized)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported userType: " + value
                        + ". Supported values are job_seeker/求职者 and recruiter/招聘者."));
    }
}
