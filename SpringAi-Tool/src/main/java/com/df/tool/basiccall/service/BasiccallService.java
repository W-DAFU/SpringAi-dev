package com.df.tool.basiccall.service;

import com.df.tool.basiccall.tool.BasiccallTool;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;



@RequiredArgsConstructor
@Service
public class BasiccallService {


    private final OpenAiChatModel openAiChatModel;
    private final BasiccallTool basiccallTool;

    /**
     * 普通模型调用示例：只把用户问题交给模型，不提供任何工具。
     */
    public String chatMessage(String message) {
        String result=ChatClient.create(openAiChatModel)
                .prompt(message)
                .call()
                .content();
        return result;
    }

    /**
     * 模型 + 工具调用示例。
     *
     * <p>tools(basiccallTool) 会把 BasiccallTool 中带 @Tool 的方法暴露给模型。
     * 当模型判断用户问题需要工具结果时，会先调用工具，再把工具返回值合成最终回答。</p>
     */
    public String chatMessageWithTools(String message) {
        String result = ChatClient.create(openAiChatModel)
                .prompt(message)
                .tools(basiccallTool)
                .call()
                .content();
        return result;
    }



}
