package com.df.tool.cardresponse.controller;

import com.df.tool.cardresponse.domain.CardChatRequest;
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
    void chatAcceptsUserTypeAndMessage() throws Exception {
        CardResponseService cardResponseService = mock(CardResponseService.class);
        CardChatRequest request = new CardChatRequest("resume", "build my resume");
        when(cardResponseService.chat(eq(request))).thenReturn("resume card");
        MockMvc mockMvc = standaloneSetup(new CardResponseController(cardResponseService)).build();

        mockMvc.perform(post("/api/card/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userType":"resume","message":"build my resume"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("resume card"));

        verify(cardResponseService).chat(eq(request));
    }
}
