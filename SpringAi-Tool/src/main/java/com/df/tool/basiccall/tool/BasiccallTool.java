package com.df.tool.basiccall.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class BasiccallTool {

    /**
     * 给大模型调用的本地工具示例。
     *
     * <p>方法加上 {@link Tool} 后，传给 ChatClient 的 tools(...) 方法时，
     * Spring AI 会把它转换成模型可识别的工具定义。模型判断需要当前时间时，
     * 会自动调用这个 Java 方法，再基于方法返回值组织最终回答。</p>
     */
    @Tool(name = "getCurrentTime", description = "Get the current date and time for a timezone.")
    public String getCurrentTime(
            @ToolParam(description = "Timezone ID, for example Asia/Shanghai or UTC.") String timezone) {
        log.info("getCurrentTime{}", timezone);
        ZoneId zoneId = ZoneId.of(timezone);
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        return "Current time in " + timezone + " is "
                + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
    }

}
