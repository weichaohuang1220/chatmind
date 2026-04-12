package com.kama.chatmind.controller;

import com.kama.chatmind.agent.tools.Tool;
import com.kama.chatmind.agent.tools.ToolType;
import com.kama.chatmind.service.ToolFacadeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ToolController.class)
class ToolControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ToolFacadeService toolFacadeService;

    @Test
    void getOptionalTools_success() throws Exception {
        Tool mockTool = new Tool() {
            @Override
            public String getName() { return "WebSearchTool"; }
            @Override
            public String getDescription() { return "Search the web"; }
            @Override
            public ToolType getType() { return ToolType.OPTIONAL; }
        };

        when(toolFacadeService.getOptionalTools()).thenReturn(List.of(mockTool));

        mockMvc.perform(get("/api/tools"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("WebSearchTool"))
                .andExpect(jsonPath("$.data[0].description").value("Search the web"));
    }

    @Test
    void getOptionalTools_empty() throws Exception {
        when(toolFacadeService.getOptionalTools()).thenReturn(List.of());

        mockMvc.perform(get("/api/tools"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void getOptionalTools_serviceThrows() throws Exception {
        when(toolFacadeService.getOptionalTools()).thenThrow(new RuntimeException("Failed"));

        mockMvc.perform(get("/api/tools"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
}
