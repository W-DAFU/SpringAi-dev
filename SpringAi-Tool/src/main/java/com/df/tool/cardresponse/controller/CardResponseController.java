package com.df.tool.cardresponse.controller;

import com.df.tool.cardresponse.domain.CardChatRequest;
import com.df.tool.cardresponse.domain.CardChatResponse;
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
     * 根据 userId、userType、message 生成完整卡片协议。
     */
    @PostMapping("chat")
    public CardChatResponse chat(@RequestBody CardChatRequest request) {
        return cardResponseService.chat(request);
    }
}
