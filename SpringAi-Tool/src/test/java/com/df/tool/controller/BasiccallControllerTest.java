package com.df.tool.controller;

import com.df.tool.basiccall.service.BasiccallService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class BasiccallControllerTest {

    @Test
    void chatAcceptsJsonMessage() throws Exception {
        BasiccallService basiccallService = mock(BasiccallService.class);
        when(basiccallService.chatMessage("hello")).thenReturn("answer");
        MockMvc mockMvc = standaloneSetup(new BasiccallController(basiccallService)).build();

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"hello"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("answer"));

        verify(basiccallService).chatMessage("hello");
    }

    @Test
    void chatWithToolsAcceptsJsonMessage() throws Exception {
        BasiccallService basiccallService = mock(BasiccallService.class);
        when(basiccallService.chatMessageWithTools("现在几点")).thenReturn("tool answer");
        MockMvc mockMvc = standaloneSetup(new BasiccallController(basiccallService)).build();

        mockMvc.perform(post("/api/chat/tools")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"现在几点"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("tool answer"));

        verify(basiccallService).chatMessageWithTools("现在几点");
    }
    @Test
    void chatWithToolAliasAcceptsJsonMessage() throws Exception {
        BasiccallService basiccallService = mock(BasiccallService.class);
        when(basiccallService.chatMessageWithTools("time")).thenReturn("tool answer");
        MockMvc mockMvc = standaloneSetup(new BasiccallController(basiccallService)).build();

        mockMvc.perform(post("/api/chat/tool")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"time"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("tool answer"));

        verify(basiccallService).chatMessageWithTools("time");
    }
}
