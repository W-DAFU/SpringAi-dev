package com.df.tool.cardresponse.controller;

import com.df.tool.cardresponse.domain.CardChatRequest;
import com.df.tool.cardresponse.domain.CardChatResponse;
import com.df.tool.cardresponse.service.CardResponseService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class CardResponseControllerTest {

    @Test
    void chatAcceptsUserIdUserTypeAndMessage() throws Exception {
        CardResponseService cardResponseService = mock(CardResponseService.class);
        CardChatRequest request = new CardChatRequest("u1001", "resume", "生成简历卡片");
        when(cardResponseService.chat(eq(request)))
                .thenReturn(new CardChatResponse("简历", "{\"name\":\"张三\"}", "已生成简历卡片"));
        MockMvc mockMvc = standaloneSetup(new CardResponseController(cardResponseService)).build();

        mockMvc.perform(post("/api/card/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":"u1001","userType":"resume","message":"生成简历卡片"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"messageType":"简历","results":"{\\"name\\":\\"张三\\"}","message":"已生成简历卡片"}
                        """));

        verify(cardResponseService).chat(eq(request));
    }
}
