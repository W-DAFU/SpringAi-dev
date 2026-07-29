package com.df.tool.cardresponse.controller;

import com.df.tool.cardresponse.domain.CardChatRequest;
import com.df.tool.cardresponse.service.CardResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 卡片返回案例接口。
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("api/card")
public class CardResponseController {

    private final CardResponseService cardResponseService;

    /**
     * 根据 userType 和 message 生成卡片回答。
     *
     * <p>userType=resume 时加载简历提示词和简历工具；
     * userType=job 时加载岗位提示词和岗位工具。</p>
     */
    @PostMapping("chat")
    public String chat(@RequestBody CardChatRequest request) {
        return cardResponseService.chat(request);
    }
}
