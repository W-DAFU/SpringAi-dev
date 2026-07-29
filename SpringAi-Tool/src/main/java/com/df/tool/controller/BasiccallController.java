package com.df.tool.controller;

import com.df.tool.basiccall.domain.BasiccallVo;
import com.df.tool.basiccall.service.BasiccallService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RestController
@RequestMapping("api/")
public class BasiccallController {


    /**
     * 基础调用案例的业务服务。
     */
    private final BasiccallService basiccallService;

    /**
     * 普通模型调用接口。
     *
     * <p>只把用户问题发送给大模型，不额外提供工具能力。</p>
     *
     * @param basiccallVo 请求体，示例：{"message":"你好"}
     * @return 大模型直接生成的回答
     */
    @PostMapping("chat")
    public String chat(@RequestBody BasiccallVo basiccallVo) {

        return basiccallService.chatMessage(basiccallVo.message());
    }

    /**
     * 模型 + 工具调用接口。
     *
     * <p>把用户问题发送给大模型，同时暴露 BasiccallTool 中带 @Tool 的方法。
     * 当模型判断需要工具结果时，会先调用本地 Java 工具，再组织最终回答。</p>
     *
     * @param basiccallVo 请求体，示例：{"message":"现在北京时间几点"}
     * @return 大模型结合工具结果生成的回答
     */
    @PostMapping({"chat/tool", "chat/tools"})
    public String chatWithTools(@RequestBody BasiccallVo basiccallVo) {

        return basiccallService.chatMessageWithTools(basiccallVo.message());
    }


}
