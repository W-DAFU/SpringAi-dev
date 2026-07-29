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
    void chatAcceptsUserIdentityUserIdAndMessage() throws Exception {
        CardResponseService cardResponseService = mock(CardResponseService.class);
        CardChatRequest request = new CardChatRequest("u1001", "job_seeker", "帮我找岗位");
        when(cardResponseService.chat(eq(request)))
                .thenReturn(new CardChatResponse("岗位", "{\"title\":\"Java后端开发工程师\"}", "已经为你找到匹配岗位。"));
        MockMvc mockMvc = standaloneSetup(new CardResponseController(cardResponseService)).build();

        mockMvc.perform(post("/api/card/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":"u1001","userType":"job_seeker","message":"帮我找岗位"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"messageType":"岗位","results":"{\\"title\\":\\"Java后端开发工程师\\"}","message":"已经为你找到匹配岗位。"}
                        """));

        verify(cardResponseService).chat(eq(request));
    }
}
